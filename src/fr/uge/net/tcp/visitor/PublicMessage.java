package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PublicMessage implements Frame{

	private final String sender;
	private final String message;
	
	public PublicMessage(String sender, String message){
		this.sender = Objects.requireNonNull(sender);
		this.message = Objects.requireNonNull(message);
	}
	
	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}


	public String getMessage() {
		return message;
	}

	public String getSender() {
		return sender;
	}
}
