package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.Response.Codes;

public class OpCodeProcess implements ProcessInt {

	private final IntReader intReader = new IntReader();
	private boolean receivedCode = false;


	public boolean process(ByteBuffer bbin) {
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

	public boolean receivedCode() {
		return receivedCode;
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
	
	@Override
	public String getLogin() {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");
	}


	public Integer getValue() {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");

	}

	@Override
	public String getTargetLogin() {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");
	}

	@Override
	public void reset() {
		receivedCode = false;
		intReader.reset();

	}
	
	@Override
	public String getMessage() {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");

	}

	@Override
	public long getId() {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");

	}

	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		throw new UnsupportedOperationException("operation not valide for OpCodeProcess");
		
	}


}
