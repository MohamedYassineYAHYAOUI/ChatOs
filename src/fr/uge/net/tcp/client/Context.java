package fr.uge.net.tcp.client;

import java.io.IOException;

interface Context {
	
	/**
	 * Connect the client
	 * 
	 * @throws IOException
	 */
	void doConnect() throws IOException  ;
	
	/**
	 * Writes in the buffer
	 * 
	 * @throws IOException
	 */
	void doWrite() throws IOException ;
	
	/**
	 * Reads the buffer
	 * 
	 * @throws IOException
	 */
	void doRead() throws IOException ;
	
	/**
	 * Sets boolean for the connection
	 * 
	 * @param value the boolean
	 */
	void setConnected(boolean value);
}
