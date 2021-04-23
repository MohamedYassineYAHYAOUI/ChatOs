package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PrivateConnexionAccepted extends Frame{

	private final String receiver; 
	private final String sender;
	private Long id;
	
	public PrivateConnexionAccepted(String sender, String receiver, Long id) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
		this.id = id;
	}
	
	public PrivateConnexionAccepted(String sender, String receiver) {
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
		id = null;
	}
	
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	public Long getId() {
		if( id == null) {
			throw new IllegalStateException("request with no id");
		}
		return id;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSender() {
		return sender;
	}

}
