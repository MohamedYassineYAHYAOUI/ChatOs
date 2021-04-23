package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.uge.net.tcp.readers.IntReader;
import fr.uge.net.tcp.responses.Response.Codes;

public class OpCodeProcess implements Process {

	private final IntReader intReader = new IntReader();
	private boolean receivedCode = false;


	/**
	 * Treats the process of the byte buffer gives in parameter
	 * 
	 * @param bbin
	 * @return true if the process is done, and false in the others cases
	 */
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

	/**
	 * Checks if a code was received
	 * 
	 * @return true if the code was received and false in others cases
	 */
	public boolean receivedCode() {
		return receivedCode;
	}
	
	/**
	 * Gets the process code
	 * 
	 * @return the process code
	 */
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


	/**
	 * Gets the invalid value
	 * 
	 * @return the invalid integer for op code process
	 */
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
