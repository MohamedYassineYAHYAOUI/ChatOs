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

import fr.uge.net.tcp.responses.Response;
import fr.uge.net.tcp.responses.Response.Codes;
import fr.uge.net.tcp.visitor.Frame;
import fr.uge.net.tcp.frameReaders.FrameReader;




class Context {

	static private final int BUFFER_SIZE = 1_024;
	static private final Logger logger = Logger.getLogger(Context.class.getName());

	private final SelectionKey key;
	private final SocketChannel sc;
	private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	
	private final Queue<Response> queue = new LinkedList<>();
	private final ServerOperations serverOperations;
	private final FrameReader frameReader;
	private final ServerFrameVisitor frameVisitor;
	private boolean mainSocketForClient = false;

	private Context  privateConnetion; // context
	private boolean closed = false;

	Context(Server server, SelectionKey key, ServerOperations serverOperations){
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.serverOperations = serverOperations;
		this.frameReader = new FrameReader();
		this.frameVisitor = new ServerFrameVisitor(server, this, serverOperations);
	}
	
	
	
	/**
	 * process the bbin of the context 
	 * bbin is in write mode at the beginning and the end of the method
	 */
	private void processIn() {
		for(;;) {
			var status = frameReader.process(bbin);
			switch(status) {
			case ERROR:
				silentlyClose();
				return;
			case REFILL:
				return;
			case DONE:
				Frame frame = frameReader.get();
				frameReader.reset();
				treatFrame(frame);
				break;
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
	 * Set this context as the main channel for the client
	 */
	void setAsMainChannel() {
		this.mainSocketForClient = true;
	}

	
	/**
	 *  
	 * @param frame product that accepts the visitor
	 */
	private void treatFrame(Frame frame) {
		frame.accept(frameVisitor);
	}
	
	/**
	 * getter for this context socket Channel
	 * @return SocketChannel
	 */
	SocketChannel contextSocketChannel() {
		return sc;
	}
	
	/**
	 * Try to fill bbout from the message queue
	 * bbout stays in write in this method
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
	 * Try to fill bbout from the ByteBuffer bb
	 * bbout stays in write in this method
	 *@param bb ByteBuffer to fill bbout from
	 */	
	private void processOutPrivate(ByteBuffer bb) {
		if(bbout.remaining() >= bb.position()) {
			bbout.put(bb.flip());
			bb.compact();
		}
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
			serverOperations.removeClient(sc);
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
