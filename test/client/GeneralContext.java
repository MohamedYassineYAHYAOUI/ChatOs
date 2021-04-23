package fr.uge.net.tcp.client;

import java.io.IOException;

interface GeneralContext {
	
	void doConnect() throws IOException  ;
	
	void doWrite() throws IOException ;
	
	void doRead() throws IOException ;
}
