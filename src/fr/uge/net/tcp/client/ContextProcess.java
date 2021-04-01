package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.reader.IntReader;
import fr.uge.net.tcp.reader.MessageReader;
import fr.uge.net.tcp.reader.StringReader;

class ContextProcess {

	static private final Logger logger = Logger.getLogger(ContextProcess.class.getName());

	private final IntReader intReader = new IntReader();
	private final MessageReader messageReader = new MessageReader();
	private final StringReader stringReader = new StringReader();
	private final SocketChannel sc;

	private boolean receivedCode = false;

	ContextProcess(SocketChannel sc) {
		this.sc = Objects.requireNonNull(sc);
	}

	void publicMessageProcess(ByteBuffer bbin) {
		switch(messageReader.process(bbin)) {
		case DONE:
			System.out.println(messageReader.getLogin()+": "+messageReader.getMessage());
			messageReader.reset();
			restCode();
			break;
		case REFILL:
			return;
		case ERROR:
			logger.log(Level.WARNING, "error processing public message from server" );
			silentlyClose();
			return;
		}
	}

	boolean processCode(ByteBuffer bbin) throws IOException {

		if (!receivedCode) {
			switch (intReader.process(bbin)) {
			case DONE:
				receivedCode = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				logger.log(Level.WARNING, "error processing code for client " + sc.getRemoteAddress());
				silentlyClose();
				return false;
			default:
				return false;
			}
		}
		return true;
	}

	void restCode() {
		receivedCode = false;
		intReader.reset();
	}

	int getCode() {
		return intReader.get();
	}

	void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

}
