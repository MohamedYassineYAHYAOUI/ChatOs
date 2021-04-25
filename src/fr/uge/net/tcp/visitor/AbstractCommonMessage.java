package fr.uge.net.tcp.visitor;

import java.util.Objects;

/**
 * Abstract class for the common code
 */
abstract class AbstractCommonMessage {

	final String receiver; 
	final String sender;
	
	AbstractCommonMessage( String sender, String receiver) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
	}
	
	/**
	 * Gets the client login receiver
	 * 
	 * @return the login receiver
	 * */
	public String getReceiver() {
		return receiver;
	}
	/**
	 * Gets the client login sender
	 * 
	 * @return the login sender
	 * */
	public String getSender() {
		return sender;
	}
}
