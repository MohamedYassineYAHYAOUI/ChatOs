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
	private final long id;
	
	private PrivateContext(SelectionKey key, String targetLogin, String initialMsg, long id) {
		super(key);
		this.targetLogin = Objects.requireNonNull(targetLogin);
		this.initialMsg = initialMsg;
		this.id =id;
	}

	static PrivateContext CreateContext(SelectionKey key, String targetLogin, long id, String initialMsg) {
		var pc = new PrivateContext(key, targetLogin, initialMsg, id);

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
			System.out.println("taille bb avant "+bb);
			bb.flip();
			queue.add(bb);
			processOut();
			bb.compact();
			updateInterestOps();
			System.out.println("taille bb aprés "+bb);
		}
	}

	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	private void processOut() {
		while (!queue.isEmpty() && established) {
			System.out.println("dans while");
			var bb = queue.peek();
			if (bb.remaining() <= bbout.remaining()) {
				System.out.println("dans if");
				queue.remove();
				bbout.put(bb);
				System.out.println(bbout);
			} else {
				System.out.println("dans break ");
				break;
			}
		}
	}

	long getId() {
		return id;
	}
	
	
	private void processIn() throws IOException {
		if (closed) {
			return;
		}
		if(established) {
			System.out.println("CONNEXION ETABLI");
			bbin.flip();
			System.out.println("> message from " + targetLogin + " on the private connexion:" +UTF8.decode(bbin).toString());
			bbin.compact();
		}else {
			try {
				if(!codeProcess.process(bbin)) {
					return;
				}
				System.out.println("connexion non etabli");
				if(codeProcess.receivedCode() && ( codeProcess.getProcessCode() == Codes.ESTABLISHED)) {

					established = true;
					if (initialMsg != null) {
						System.out.println("DIFFRENT FORM NULL");
						var bb = ByteBuffer.allocate(Character.BYTES * initialMsg.length());
						bb.put(UTF8.encode(initialMsg));
						queueMessage(bb);
					}
				}
			}catch(IllegalStateException e) {
				silentlyClose();
				closed = true;
			}finally {
				codeProcess.reset();
			}
		}
	}
	

	public void doRead() throws IOException {
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

	public void doWrite() throws IOException {

		bbout.flip();

		sc.write(bbout);
		bbout.compact();
		processOut();
		
		updateInterestOps();
	}

}
