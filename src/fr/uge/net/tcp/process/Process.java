package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;

public interface Process {

	String getLogin();
	
	String getMessage();
	
	long getId();
	
	String getTargetLogin();
	
	void reset();

	boolean executeProcess(ByteBuffer bbin);

}
