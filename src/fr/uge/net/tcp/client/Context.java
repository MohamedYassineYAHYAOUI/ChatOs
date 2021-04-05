package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Logger;

import fr.uge.net.tcp.process.GenericValueProcess;
import fr.uge.net.tcp.process.LongReader;
import fr.uge.net.tcp.process.MessageProcess;
import fr.uge.net.tcp.process.OpCodeProcess;
import fr.uge.net.tcp.process.Process;
import fr.uge.net.tcp.process.ProcessInt;
import fr.uge.net.tcp.server.replies.MessageResponse;
import fr.uge.net.tcp.server.replies.Response.Codes;

class Context {

	static private final int BUFFER_SIZE = 1_024;
	static private final Logger logger = Logger.getLogger(Context.class.getName());

	final private SelectionKey key;
	final private SocketChannel sc;
	final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
	final private PrivateConnectionTraitement pcTraitaitement;
	
	//final private HashMap<String, Long> clientsWithPrivateConnexion = new HashMap<>();
	//hashmap <String , simpleEntry<Long, SC > >
	
	
	private final Process process;
	private final OpCodeProcess codeProcess = new OpCodeProcess();
	private ProcessInt processInt;
	private boolean doneProcessing = true;

	private final ClientOS clientOs;

	private final String login;

	private boolean closed = false;
	private boolean isConnected = false;

	Context(SelectionKey key, String login, ClientOS clientOs, PrivateConnectionTraitement pcTraitaitement) {
		this.login = Objects.requireNonNull(login);
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.process = new Process();
		this.clientOs = clientOs;
		this.pcTraitaitement = Objects.requireNonNull(pcTraitaitement);
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
	private void processIn() throws IOException {
		// read opcode
		if (closed) {
			return;
		}

		try {

			if (!codeProcess.process(bbin)) {
				return;
			}
			/*
			 * if (!process.processCode(bbin)) { return; }
			 */
			if (codeProcess.receivedCode() && doneProcessing) {
				switch (codeProcess.getProcessCode()) {
				case LOGIN_ACCEPTED:
					logger.info("connection to server established");
					isConnected = true;
					codeProcess.reset();
					return;
				case LOGIN_REFUSED:
					throw new IllegalStateException();
				case PUBLIC_MESSAGE_RECEIVED:
					processInt = new MessageProcess(codeProcess,
							(login, msg) -> System.out.println(login + ": " + msg));
					break;
				case PRIVATE_MESSAGE_RECEIVED:
					processInt = new MessageProcess(codeProcess,
							(login, msg) -> System.out.println("private message from " + login + ": " + msg));
					break;
				case REQUEST_PRIVATE_CONNEXION:
					processInt = new MessageProcess(codeProcess,
							(login, target) -> clientOs.requestPrivateConnection(login, target));
					break;
				case REFUSE_PRIVATE_CONNEXION:
					processInt = new MessageProcess(codeProcess, (requester, target) -> {
						if (!requester.equals(this.login)) {
							return;
						}
						
						//historique[targetlogin] = true
						//notifyAll
						System.out.println("private connexion request refused from " + target);
					});
					break;
				case ID_PRIVATE:
					processInt = new GenericValueProcess<>(codeProcess,
							new LongReader(), (requester, target, id)->	

					//synchronized 	
						//création de SC ////clientsWithPrivateConnexion.put(key, value)
						//historique[targetlogin] = true
					//notifyAll
					
					
					
					System.out.println("login " + requester + " login_target " + target + " connect id " + id));

					break;
				default:
					throw new IllegalArgumentException("invalid Code ");
				}
				
				doneProcessing = processInt.executeProcess(bbin);
				
			}
/*
			// traitement

			switch (process.getProcessCode()) {
			case LOGIN_ACCEPTED:
				logger.info("connection to server established");
				isConnected = true;
				process.reset();
				break;
			case LOGIN_REFUSED:
				throw new IllegalStateException();

			case PUBLIC_MESSAGE_RECEIVED:
				if (process.processPacket(bbin)) {
					System.out.println(process.getLogin() + ": " + process.getMessage());
					process.reset();
				}

				break;
			case PRIVATE_MESSAGE_RECEIVED:
				if (process.processPacket(bbin)) {
					System.out.println("private message from " + process.getLogin() + ": " + process.getMessage());
					process.reset();
				}
				break;
			case REQUEST_PRIVATE_CONNEXION:
				if (process.processPrivateConnextion(bbin)) {
					clientOs.requestPrivateConnection(process.getLogin(), process.getTargetLogin());
					process.reset();
				}
				break;

			case REFUSE_PRIVATE_CONNEXION:
				if (process.processPrivateConnextion(bbin)) {
					var login_requester = process.getLogin();
					var login_target = process.getTargetLogin();
					if (!login_target.equals(login)) {
						return;
					}
					System.out.println("private connexion request refused from " + login_requester);
					process.reset();
				}
				break;
			case ID_PRIVATE:
				System.out.println("CLIENT ACCEPTED before");
				if (process.processIdPrivate(bbin)) {
					System.out.println("CLIENT ACCEPTED middle");

					var login_requester = process.getLogin();
					var login_target = process.getTargetLogin();
					var connect_id = process.getMessage();
					// clientOs -> LOGIN_PRIVATE(9)
					System.out.println(
							"login " + login_requester + " login_target " + login_target + " connect id " + connect_id);
					process.reset();
				}
				break;
			default:
				throw new IllegalArgumentException("invalid Code ");
			}*/
		} catch (IllegalArgumentException e) {
			logger.warning(e.getMessage());
			process.reset();

		} catch (IllegalStateException e) {
			process.reset();
			silentlyClose();
			closed = true;
			key.cancel();
		}

	}



	private void silentlyClose() {
		try {
			logger.info("Closing Channel");
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

	/**
	 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
	 *
	 * @param bb
	 */
	void queueMessage(ByteBuffer bb) {
		bb.flip();
		queue.add(bb);
		processOut();
		bb.compact();
		updateInterestOps();
	}

	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	private void processOut() {
		while (!queue.isEmpty()) {
			var bb = queue.peek();
			if (bb.remaining() <= bbout.remaining()) {
				queue.remove();
				bbout.put(bb);
			} else {
				break;
			}
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
		var interesOps = 0;
		if (!closed && bbin.hasRemaining()) {
			interesOps = interesOps | SelectionKey.OP_READ;
		}
		if (bbout.position() != 0) {
			interesOps |= SelectionKey.OP_WRITE;
		}
		if (interesOps == 0) {
			silentlyClose();
			return;
		}
		key.interestOps(interesOps);
	}

	boolean isConnected() {
		return isConnected;
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
		processIn();
		updateInterestOps();
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
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		processOut();
		updateInterestOps();
	}

	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
