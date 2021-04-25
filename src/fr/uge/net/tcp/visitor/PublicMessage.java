package fr.uge.net.tcp.visitor;

import java.util.Objects;


public class PublicMessage implements Frame{

	private final String sender;
	private final String message;
	
	public PublicMessage(String sender, String message){
		this.sender = Objects.requireNonNull(sender);
		this.message = Objects.requireNonNull(message);
	}
	
	/**
	 * Accept method for visiting the PublicMessage object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the PublicMessage object
	 * */	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	/**
	 * Gets the public message
	 * 
	 * @return the public message
	 * */
	public String getMessage() {
		return message;
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
