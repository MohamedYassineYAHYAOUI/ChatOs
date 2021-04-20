package fr.uge.net.tcp.client2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.Objects;

import fr.uge.net.tcp.frameReaders.FrameReader;
import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;


class PrivateContext extends CommonContext implements Context{
	static private final Charset UTF8 = Charset.forName("UTF8");
	static private final MessageResponse.Builder packetBuilder = new MessageResponse.Builder();
	
	private final FrameReader frameReader;
	private String initialMsg; // can be null
	private final String targetLogin; // other client in private connection
	private boolean established = false; // if the client is established connection yet 
	private final long id; // private connection unique id
	
	private PrivateContext(SelectionKey key, String targetLogin, String initialMsg, long id) {
		super(key);
		this.targetLogin = Objects.requireNonNull(targetLogin);
		this.initialMsg = initialMsg;
		this.id =id;
		this.frameReader = new FrameReader();
	}

	static PrivateContext CreateContext(SelectionKey key, String targetLogin, long id, String initialMsg) {
		var pc = new PrivateContext(key, targetLogin, initialMsg, id );
		
		packetBuilder.setPacketCode(Codes.LOGIN_PRIVATE).setId(id);
		var bb = packetBuilder.build().getResponseBuffer().flip();
		pc.bbout.put(bb);
		
		return pc;

	}

	/**
	 * Try to fill bbout from the message queue
	 *
	 */
	void processOut() {
		if(established) {
			super.processOut();
		}
	}
	/**
	 * @return private connection id
	 */
	long getId() {
		return id;
	}
	
	/**
	 *  process bbin for the 
	 * @throws IOException
	 */
	private void processIn() throws IOException {
		if (closed) {
			return;
		}
		if(established) {
			bbin.flip();
			System.out.println("> message from " + targetLogin + " on the private connexion: " +UTF8.decode(bbin).toString());
			bbin.compact();
		}else {
			try {
				if(!frameReader.processOpCode(bbin)) {
					return;
				}
				if(frameReader.processCode() ==  Codes.ESTABLISHED) {
					established = true;
					if (initialMsg != null) {
						var bb = ByteBuffer.allocate(Character.BYTES * initialMsg.length());
						bb.put(UTF8.encode(initialMsg));
						queueMessage(bb);
					}
				}
			}catch(IllegalStateException e) {
				silentlyClose();
				closed = true;
			}finally {
				frameReader.reset();
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

	@Override
	public void setConnected(boolean value) {
		established = value;
	}
}
