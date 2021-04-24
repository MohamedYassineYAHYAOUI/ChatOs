package fr.uge.net.tcp.visitor;

import java.util.Objects;


public class PrivateMessage extends AbstractCommonMessage implements Frame {
	

	private final String message;
	
	public PrivateMessage(String sender, String receiver, String message) {
		super(sender, receiver);
		this.message = Objects.requireNonNull(message);
	}
	
	/**
	 * Accept method for visiting the PrivateMessage object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the PrivateMessage object
	 * */
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	/**
	 * Gets the private message
	 * 
	 * @return the private message
	 * */
	public String getMessage() {
		return message;
	}

	
}
