package fr.uge.net.tcp.process;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.replies.Response.Codes;

public class Process {

	private final Logger logger = Logger.getLogger(Process.class.getName());
	private final IntReader intReader = new IntReader();
	private final MessageReader messageReader = new MessageReader();
	private final PrivateMessageReader<String> pvmessageReader = new PrivateMessageReader<String>(new StringReader());
	private final PrivateMessageReader<Long> pvmessageReaderLong = new PrivateMessageReader<Long>(new LongReader());
	final private StringReader stringReader = new StringReader();

	private boolean receivedCode = false;
	private boolean doneProcessingPacket = false;
	private boolean doneProcessingPrivatePacket = false;
	private boolean doneProcessingLogin = false;
	private boolean doneProcessingPrivateConnexion = false;
	private boolean doneProcessingID_packet = false;
	
	
	

	public boolean processPacket(ByteBuffer bbin) {
		assert (!doneProcessingPrivatePacket && !doneProcessingLogin);
		Objects.requireNonNull(bbin);
		if (!doneProcessingPacket) {
			switch (messageReader.process(bbin)) {
			case DONE:
				doneProcessingPacket = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing packet for client ");
			}
		}
		return true;
	}
	
	
	public boolean processPrivateConnextion(ByteBuffer bbin) {
		assert(!doneProcessingPacket && !doneProcessingPrivatePacket && !doneProcessingLogin);
		Objects.requireNonNull(bbin);
		if(!doneProcessingPrivateConnexion) {
			switch(messageReader.process(bbin)) {
			case DONE:
				doneProcessingPrivateConnexion = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing privte connexion packet from client ");
			}
		}
		return true;
	}
	

	public boolean processLogin(ByteBuffer bbin) {
		assert(!doneProcessingPrivatePacket && !doneProcessingPacket && !doneProcessingPrivateConnexion);
		Objects.requireNonNull(bbin);
		if (!doneProcessingLogin) {
			switch (stringReader.process(bbin)) {
			case DONE:
				doneProcessingLogin = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing Login for client ");
			}
		}
		return true;
	}
	
	
	public boolean processPrivatePacket(ByteBuffer bbin) {
		assert(!doneProcessingLogin && !doneProcessingPacket && !doneProcessingPrivateConnexion);
		Objects.requireNonNull(bbin);
		if(!doneProcessingPrivatePacket) {
			switch(pvmessageReader.process(bbin)) {
			case DONE:
				doneProcessingPrivatePacket =true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing privte packet for client ");
			}
		}
		return true;
	}
	
	
	public boolean processIdPrivate(ByteBuffer bbin) {
		assert(!doneProcessingLogin && !doneProcessingPacket && !doneProcessingPrivateConnexion);
		Objects.requireNonNull(bbin);
		if(!doneProcessingID_packet) {
			switch(pvmessageReaderLong.process(bbin)) {
			case DONE:
				doneProcessingID_packet =true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing privte packet for client ");
			}
		}
		return true;
	}
	
	

	
	
	
	

	public boolean processCode(ByteBuffer bbin) throws IOException {
		assert(!doneProcessingPacket && !doneProcessingPrivatePacket &&
				!doneProcessingLogin && !doneProcessingPrivateConnexion);
		Objects.requireNonNull(bbin);
		if (!receivedCode) {
			switch (intReader.process(bbin)) {
			case DONE:
				receivedCode = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing code for client ");

			default:
				return false;
			}
		}
		return true;
	}

	public Codes getProcessCode() {
		var value = intReader.get();
		for (var code : Codes.values()) {
			if (code.getCode() == value) {
				return code;
			}
		}
		throw new IllegalArgumentException("invlaide packet code " + value);
	}

	public String getLogin() {
		if (doneProcessingLogin) {
			return stringReader.get();
		} else if (doneProcessingPacket || doneProcessingPrivateConnexion) {
			return messageReader.getLogin();
		} else if (doneProcessingPrivatePacket ) {
			return pvmessageReader.getSenderLogin();
		}else if(doneProcessingID_packet) {
			return pvmessageReaderLong.getSenderLogin();
		}
		throw new IllegalStateException("current Process didn't finish");
	}
	
	
	public String getMessage() {
		if(doneProcessingPacket) {
			return messageReader.getMessage();
		} else if (doneProcessingPrivatePacket) {
			return pvmessageReader.getMessage();
		}else if(doneProcessingID_packet) {
			return pvmessageReaderLong.getMessage().toString();
		}
		throw new IllegalStateException("current packet process didn't finish");
	}

	
	public String getTargetLogin() {
		if(doneProcessingPrivatePacket) {
			return pvmessageReader.getTargetLogin();
		}else if(doneProcessingPrivateConnexion ) {
			return messageReader.getMessage();
		}else if(doneProcessingID_packet) {
			return pvmessageReaderLong.getTargetLogin();
		}
		throw new IllegalStateException("current packet process didn't finish");
	}
	
	
	public boolean receivedCode() {
		return receivedCode;
	}

	public void reset() {
		receivedCode = false;
		
		intReader.reset();
		messageReader.reset();
		pvmessageReader.reset();
		stringReader.reset();
		pvmessageReaderLong.reset();

		doneProcessingPacket = false;
		doneProcessingPrivatePacket = false;
		doneProcessingLogin = false;
		doneProcessingPrivateConnexion = false;
		doneProcessingID_packet = false;
	}

}
