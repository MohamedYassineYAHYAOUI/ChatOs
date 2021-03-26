package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import fr.uge.net.tcp.server.Reader.ProcessStatus;

class ClientContext {
	static private int BUFFER_SIZE = 1_024;
	static private Charset UTF8 = Charset.forName("UTF8");
	
	final private SelectionKey key;
	final private SocketChannel sc;
	final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
	final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
	
	final private Queue<Message> queue = new LinkedList<>();
	final private Server server;
	final private MessageReader messageReader = new MessageReader();
	final private IntReader intReader = new IntReader();
	final private  Operation operation;
	
	private boolean closed = false;
	private boolean acceptedAsClient = false;
	private ProcessStatus processing = ProcessStatus.REFILL;
	
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
	 */
	void processIn() {
		if(closed) {
			return;
		}	
		//processing = messageReader.process(bbin);
		//processing = intReader.process(bbin);
		
		
		if( processing == ProcessStatus.DONE) {
			
			//queueContext -> erreur (11)
			//server.broadcast(messageReader.get());
			//serve
			messageReader.reset();
			
			
			
			
		}else if( processing == ProcessStatus.REFILL) {
			return;
		}else {
			silentlyClose();
			return;
		}
	}
	
	
	private void silentlyClose() {
		try {
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
			System.out.println(">>>intrest OP : READ");
		}
		if ((bbout.position() != 0 || !queue.isEmpty())  && bbout.hasRemaining()){
			intrestOps |= SelectionKey.OP_WRITE;
			System.out.println(">>>intrest OP : WRITE");
		}
		if (intrestOps == 0) {
			silentlyClose();
		} else {
			System.out.println(">>>intrest OP "+intrestOps);
			key.interestOps(intrestOps);
		}
	}

	
}
