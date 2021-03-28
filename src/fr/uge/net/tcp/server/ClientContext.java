package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.Reader.ProcessStatus;
import fr.uge.net.tcp.server.replies.Response;
import fr.uge.net.tcp.server.replies.Response.ResponseCodes;

class ClientContext {

	static private final int BUFFER_SIZE = 1_024;
	// static private final Charset UTF8 = Charset.forName("UTF8");
	static private final Logger logger = Logger.getLogger(ClientContext.class.getName());

	final private SelectionKey key;
	final private SocketChannel sc;
	final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);

	final private Queue<Response> queue = new LinkedList<>();
	final private Server server;
	//final private MessageReader messageReader = new MessageReader();
	final private IntReader intReader = new IntReader();
	// final private Operation operation;

	private boolean closed = false;
	private boolean acceptedAsClient = false;
	private boolean receivedCode = false;

	ClientContext(Server server, SelectionKey key) {

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

		if (!receivedCode) {
			switch (intReader.process(bbin)) {
			case DONE:
				receivedCode = true;
				break;
			case REFILL:
				return;
			case ERROR:
				logger.log(Level.WARNING, "error processing code for client " + sc.getRemoteAddress());
				silentlyClose();
				return;
			}
		}
		//TODO changer les int dans les cases en ResponseCode
		if (receivedCode) {
			switch (intReader.get()) {
			case 0:
				var stringReader = new StringReader();
				if (stringReader.process(bbin) == ProcessStatus.DONE) {
					server.registerLogin(stringReader.get(), key);
					intReader.reset();
					receivedCode = false;
				}
				break;
			case 3:
				

				break;
			default:
				logger.log(Level.WARNING, "Invalide packet code from client " + sc.getRemoteAddress());
			}

		}
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

	private void silentlyClose() {
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
				if (response.getResponseCode() == ResponseCodes.LOGIN_REFUSED) {
					closed = true;
				}
			} else {
				break;
			}
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

		processIn();
		updateInterestOps();
	}

}
