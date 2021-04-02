package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;

public interface ProcessInt {
	
	boolean receivedCode = false;
	default boolean receivedCode() {
		return receivedCode;
	}
	
	boolean process(ByteBuffer bbin);
	
	String getLogin();
	
	String getMessage();
	
	String getTargetLogin();
	
	void reset();
}
