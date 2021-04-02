package fr.uge.net.tcp.reader;

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
	private final PrivateMessageReader pvmessageReader = new PrivateMessageReader();
	final private StringReader stringReader = new StringReader();

	private boolean receivedCode = false;
	private boolean doneProcessingPacket = false;
	private boolean doneProcessingPrivatePacket = false;
	private boolean doneProcessingLogin = false;

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

	public boolean processLogin(ByteBuffer bbin) {
		assert(!doneProcessingPrivatePacket && !doneProcessingPacket);
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
		assert(!doneProcessingLogin && !doneProcessingPacket);
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

	public boolean processCode(ByteBuffer bbin) throws IOException {
		assert(!doneProcessingPacket && !doneProcessingPrivatePacket && !doneProcessingLogin);
		Objects.requireNonNull(bbin);
		if (!receivedCode) {
			switch (intReader.process(bbin)) {
			case DONE:
				receivedCode = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				// logger.log(Level.WARNING, );
				throw new IllegalStateException("error processing code for client ");
			// silentlyClose();
			// return false;
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
		} else if (doneProcessingPacket) {
			return messageReader.getLogin();
		} else if (doneProcessingPrivatePacket) {
			return pvmessageReader.getSenderLogin();
		}
		throw new IllegalStateException("current Process didn't finish");
	}
	
	
	public String getMessage() {
		if(doneProcessingPacket) {
			return messageReader.getMessage();
		} else if (doneProcessingPrivatePacket) {
			return pvmessageReader.getMessage();
		}
		throw new IllegalStateException("current packet process didn't finish");
	}

	
	public String getTargetLogin() {
		return pvmessageReader.getTargetLogin();
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
		receivedCode = false;
		doneProcessingPacket = false;
		doneProcessingPrivatePacket = false;
		doneProcessingLogin = false;
		
	}

}
