package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.process.GenericValueProcess;
import fr.uge.net.tcp.process.LoginProcess;
import fr.uge.net.tcp.readers.LongReader;
import fr.uge.net.tcp.process.MessageProcess;
import fr.uge.net.tcp.process.OpCodeProcess;
import fr.uge.net.tcp.process.Process;
import fr.uge.net.tcp.readers.StringReader;
import fr.uge.net.tcp.responses.Response;
import fr.uge.net.tcp.responses.Response.Codes;

class Context {

	static private final int BUFFER_SIZE = 1_024;
	static private final Logger logger = Logger.getLogger(Context.class.getName());

	final private SelectionKey key;
	final private SocketChannel sc;
	final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	final private Queue<Response> queue = new LinkedList<>();
	final private Server server;

	private final OpCodeProcess codeProcess = new OpCodeProcess();
	private Process process;
	private boolean doneProcessing = true;
	private boolean mainSocketForClient = false;

	private Context  privateConnetion; // context
	
	


	private boolean closed = false;

	Context(Server server, SelectionKey key) {

		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.server = server;
	}
	
	
	
	/**
	 * Process the content of bbin
	 *
	 * The convention is that bbin is in write-mode before the call to process and
	 * after the call
	 * 
	 * @throws IOException
	 *
	 */
	void processIn() throws IOException {
		if (closed) {
			return;
		}
		try {	
			if(!codeProcess.process(bbin)) {
				return;
			}
			
			if(codeProcess.receivedCode() && doneProcessing) {
				switch(codeProcess.getProcessCode()) {
				case REQUEST_SERVER_CONNECTION:
					process = new LoginProcess(codeProcess, login->{
						server.registerLogin(login, key);
						mainSocketForClient = true;
					});
					break;
					
				case PUBLIC_MESSAGE_SENT:
					process = new MessageProcess(codeProcess, (login, msg)->server.broadcast(login, msg , key));
					break;
					
				case PRIVATE_MESSAGE_SENT:
					process = new GenericValueProcess<>(codeProcess, new StringReader(), 
							(login, target, msg)-> server.sendPrivateMessage(login, target, msg, key) );
					break;
				case REQUEST_PRIVATE_CONNEXION:
					process = new MessageProcess(codeProcess, 
							(login, target)-> server.redirectPrivateConnexionRequest(login, target , key, Codes.REQUEST_PRIVATE_CONNEXION, false));
					break;
				case REFUSE_PRIVATE_CONNEXION:
					process = new MessageProcess(codeProcess, 
							(login, target)-> server.redirectPrivateConnexionRequest(login, target, key, Codes.REFUSE_PRIVATE_CONNEXION, true));
					break;
					
				case ACCEPT_PRIVATE_CONNEXION:
					process = new MessageProcess(codeProcess, 
							(login, target)->server.redirectIdPacket(login, target, key));
					break;
					
				case LOGIN_PRIVATE:
					process = new LongReader(codeProcess, id-> server.establishConnection(id, key) );
					break;
				case DISCONNECT_PRIVATE:
					process = new LongReader(codeProcess, id->server.removeClient(id));
					
					break;
				default:
					logger.log(Level.WARNING, "Invalide packet code from client " + sc.getRemoteAddress());
				}
				
			}
			
			if(doneProcessing = process.executeProcess(bbin)) {
				updateInterestOps();
			}

		} catch (IllegalArgumentException e) {
			logger.warning(e.getMessage());
		} catch (IllegalStateException  | IOException  e) {
			logger.warning(e.getMessage());
			
			silentlyClose();
			closed = true;
			key.cancel(); 
		}finally {
			if(process != null) {
				process.reset();
			}
		}
	}
	
	/**
	 * Check if the current channel of a client is the main socket
	 * 
	 * return true is the main socket, and false in other case
	 */

	boolean isMainChannel() {
		return mainSocketForClient;
	}

	/**
	* Set the private context to a client for a private connection
	*
	* @param pcContext the private context
	*/
	void setPrivateConnection(Context pcContext) {
		this.privateConnetion = Objects.requireNonNull(pcContext);
	}
	/**
	 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
	 *
	 * @param msg
	 */
	void queueResponse(Response res) {
		queue.add(res);
		processOut();
		updateInterestOps();
	}

	/**
	 * Close a socket channel of a client
	 */
	void silentlyClose() {
		try {
			logger.log(Level.INFO, "closing client " + sc.getRemoteAddress());
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

	/**
	 * Update the interestOps of the key looking only at values of the boolean
	 * closed and of both ByteBuffers.
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * updateInterestOps and after the call. Also it is assumed that process has
	 * been be called just before updateInterestOps.
	 */

	private void updateInterestOps() {
		int intrestOps = 0;

		if (bbin.hasRemaining() && !closed) {
			intrestOps |= SelectionKey.OP_READ;
		}
		if ((bbout.position() != 0 || !queue.isEmpty()) && bbout.hasRemaining()) {
			intrestOps |= SelectionKey.OP_WRITE;
		}
		if (intrestOps == 0) {
			server.removeClient(key);
			silentlyClose();
			
		} else {
			key.interestOps(intrestOps);
		}
	}

	/**
	 * Performs the write action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doWrite and after the call
	 *
	 * @throws IOException
	 */

	void doWrite() throws IOException {
		processOut();
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		updateInterestOps();
	}

	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	private void processOut() {
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().size()) {
			var response = queue.poll();
			if (response != null) {
				bbout.put(response.getResponseBuffer().flip());
				if (response.getResponseCode() == Codes.LOGIN_REFUSED) {
					closed = true;
				}
			} else {
				break;
			}
		}
	}

	
	/**
	 * Try to fill bbout from the message queue for a private connection
	 *
	 */	
	private void processOutPrivate(ByteBuffer bb) {
		if(bbout.remaining() >= bb.position()) {
			System.out.println("writing in BBOUT");
			
			bbout.put(bb.flip());
			bb.compact();
		}
	}
	
	
	
	/**
	 * Performs the read action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doRead and after the call
	 *
	 * @throws IOException
	 */
	void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}
		if(!mainSocketForClient && privateConnetion!=null) {
			//privateConnetion.queueResponse(res);
			privateConnetion.processOutPrivate(bbin);
			privateConnetion.updateInterestOps();
		}else {
			processIn();
		}
		updateInterestOps();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Context)) {
			return false;
		}
		var context = (Context) obj;
		try {
			return sc.getRemoteAddress().toString().equals(context.sc.getRemoteAddress().toString());
		} catch (IOException e) {
			return false;
		}
	}

}
