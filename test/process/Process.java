package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;

public interface Process {

	/**
	 * Gets login of the sender client
	 * 
	 * @return the login
	 */
	String getLogin();

	/**
	 * Gets message of a client
	 * 
	 * @return the message
	 */
	String getMessage();

	/**
	 * Gets connect id of a private connection
	 * 
	 * @return the connect id
	 */
	long getId();

	/**
	 * Gets login of the target client
	 * 
	 * @return the login
	 */
	String getTargetLogin();

	/**
	 * Resets all the fields of the process
	 * 
	 */
	void reset();

	/**
	 * Executes the process using the data in the byte buffer gives in parameter
	 * 
	 * @param bbin
	 * @return true the process is done
	 */
	boolean executeProcess(ByteBuffer bbin);

}
