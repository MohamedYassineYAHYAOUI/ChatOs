package fr.uge.net.tcp.client2;

import java.io.IOException;

public interface Context {
	void doConnect() throws IOException  ;
	
	void doWrite() throws IOException ;
	
	void doRead() throws IOException ;
	
	void setConnected(boolean value);
}
