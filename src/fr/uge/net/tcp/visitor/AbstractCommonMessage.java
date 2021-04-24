package fr.uge.net.tcp.visitor;

import java.util.Objects;

abstract class AbstractCommonMessage {

	final String receiver; 
	final String sender;
	
	AbstractCommonMessage( String sender, String receiver) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSender() {
		return sender;
	}
}
