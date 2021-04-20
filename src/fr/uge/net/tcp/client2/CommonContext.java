package fr.uge.net.tcp.client2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;




abstract class CommonContext {
	static private final int BUFFER_SIZE = 1_024;
	static private final Logger logger = Logger.getLogger(CommonContext.class.getName());

	final SelectionKey key;
	final SocketChannel sc;
	final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	final Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode

	boolean closed = false;	
	
	CommonContext(SelectionKey key) {
		this.key = key;
		this.sc = (SocketChannel) key.channel();
	}
	
	
	void silentlyClose() {
		
		try {
			logger.info("Closing Channel");
			closed = true;
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}
	
	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	void processOut() {
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
	 * Performs the write action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doWrite and after the call
	 *
	 * @throws IOException
	 */

	public void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		processOut();
		updateInterestOps();
	}
	
	
	/**
	 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
	 *
	 * @param bb
	 */
	void queueMessage(ByteBuffer bb) {
		
		synchronized (queue) {
			bb.flip();
			queue.add(bb); //offer à la place de add
			processOut();
			bb.compact();
			updateInterestOps();
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
	
	void updateInterestOps() {
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
	
	/**
	 * set key to write if the socket channel is connected
	 * @throws IOException
	 */
	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
