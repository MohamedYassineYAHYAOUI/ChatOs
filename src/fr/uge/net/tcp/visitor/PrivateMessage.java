package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PrivateMessage extends AbstractCommonMessage implements Frame {
	

	private final String message;
	
	public PrivateMessage(String sender, String receiver, String message) {
		super(sender, receiver);
		this.message = Objects.requireNonNull(message);
	}
	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	
	public String getMessage() {
		return message;
	}

	
}
