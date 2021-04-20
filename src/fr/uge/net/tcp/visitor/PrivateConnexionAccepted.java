package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PrivateConnexionAccepted extends Frame{

	private final String receiver; 
	private final String sender;
	private final Long id;
	
	public PrivateConnexionAccepted(String sender, String receiver, Long id) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
		this.id = id;
	}
	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	public Long getId() {
		return id;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSender() {
		return sender;
	}

}
