package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.reader.Process;

class Context {

	static private final int BUFFER_SIZE = 1_024;
	private static final Charset UTF8 = Charset.forName("utf8");
	static private final Logger logger = Logger.getLogger(Context.class.getName());

	final private SelectionKey key;
	final private SocketChannel sc;
	final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	final private Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode

	private final Process process;

	private boolean closed = false;
	private boolean isConnected = false;

	Context(SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
		this.process = new Process();
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
			if (!process.processCode(bbin)) {
				return;
			}

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
				if(process.processPacket(bbin)) {
					System.out.println(process.getLogin()+": "+process.getMessage());
					process.reset();
				}

				break;
			case PRIVATE_MESSAGE_RECEIVED:
				if(process.processPacket(bbin)) {
					System.out.println("private message from "+process.getLogin()+": "+process.getMessage());
					process.reset();
				}
				break;
			default:
				throw new IllegalArgumentException("invlaid Code ");
			}
		} catch (IllegalArgumentException e) {
			logger.warning(e.getMessage());
			process.reset();

		} catch (IllegalStateException e) {
			process.reset();
			silentlyClose();
			closed = true; // ??? a voir
			key.cancel(); // ??? à voir
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
