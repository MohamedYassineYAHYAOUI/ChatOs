package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PrivateMessage extends Frame {
	
	private final String receiver; 
	private final String sender;
	private final String message;
	
	public PrivateMessage(String sender, String receiver, String message) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
		this.message = Objects.requireNonNull(message);
	}
	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	
	public String getMessage() {
		return message;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSender() {
		return sender;
	}
	
}
