package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.replies.MessageResponse;
import fr.uge.net.tcp.server.replies.Response.Codes;

class PrivateContext extends CommonContext implements GeneralContext{

	static private final Logger logger = Logger.getLogger(Context.class.getName());
	static private final Charset UTF8 = Charset.forName("UTF8");
	static private final MessageResponse.Builder packetBuilder = new MessageResponse.Builder();
	
	private String initialMsg; // can be null
	private final String targetLogin;
	private boolean established = false;

	private PrivateContext(SelectionKey key, String targetLogin, String initialMsg) {
		super(key);
		this.targetLogin = Objects.requireNonNull(targetLogin);
		this.initialMsg = initialMsg;
	}

	static PrivateContext CreateContext(SelectionKey key, String targetLogin, long id, String initialMsg) {
		var pc = new PrivateContext(key, targetLogin, initialMsg);

		packetBuilder.setPacketCode(Codes.LOGIN_PRIVATE).setId(id);
		var bb = packetBuilder.build().getResponseBuffer().flip();
		pc.bbout.put(bb);
		
		//pc.queueMessage(packetBuilder.build().getResponseBuffer());
		return pc;

	}

	/**
	 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
	 *
	 * @param bb
	 */
	void queueMessage(ByteBuffer bb) {
		synchronized (queue) {
			bb.flip();
			queue.add(bb);
			processOut();
			bb.compact();
			updateInterestOps();
		}
	}

	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	private void processOut() {
		while (!queue.isEmpty() && established) {
			var bb = queue.peek();
			if (bb.remaining() <= bbout.remaining()) {
				queue.remove();
				bbout.put(bb);
			} else {
				break;
			}
		}
	}

	private void processInNotEstablished() throws IOException {
		if(closed) {
			return;
		}
		try {
			if(!codeProcess.process(bbin)) {
				return;
			}
			System.out.println("connexion non etabli");
			if(codeProcess.receivedCode() && ( codeProcess.getProcessCode() == Codes.ESTABLISHED)) {
				if (initialMsg != null) {
					var bb = ByteBuffer.allocate(Integer.BYTES + (Character.BYTES * initialMsg.length()));
					bb.put(UTF8.encode(initialMsg));
					queueMessage(bb);
				}
				established = true;
			}

		}catch(IllegalStateException e) {

			silentlyClose();
			closed = true;
		}finally {
			codeProcess.reset();
		}
			

	}

	private void processInEstablished() throws IOException {
		if (closed) {
			return;
		}
		var strB = new StringBuilder();
		bbin.flip();
		while (true) {
			var tmp = (char) bbin.get();
			strB.append(tmp);
			if (tmp == '\n') {
				break;
			}
		}
		bbin.compact();
		System.out.println("> message from " + targetLogin + " on the private connexion:" + strB);
	}

	public void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}
		if(established) {
			processInEstablished();
		}else {
			processInNotEstablished();
		}
		
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

	public void doWrite() throws IOException {
		System.out.println(" 664");
		bbout.flip();
		System.out.println(" qvfefv");
		if(sc == null) {
			System.out.println("socket channel ");
		}
		if(bbout == null) {
			System.out.println("BBOUT");
		}
		sc.write(bbout);
		System.out.println(" qvfefv");
		bbout.compact();
		processOut();
		
		System.out.println("Sdfvefv");
		updateInterestOps();
	}

}
