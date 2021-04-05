package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;

public interface ProcessInt {
	
	//boolean receivedCode = false;
	/*default boolean receivedCode() {
		return receivedCode;
	}*/
	

	
	String getLogin();
	
	String getMessage();
	
	long getId();
	
	String getTargetLogin();
	
	void reset();

	boolean executeProcess(ByteBuffer bbin);

}