package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class PrivateConnexionRequest extends Frame {

	private final String receiver; 
	private final String sender;
	
	
	public PrivateConnexionRequest( String sender, String receiver){
		this.receiver = Objects.requireNonNull(receiver);
		this.sender = Objects.requireNonNull(sender);
	}
	
	
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	
	public String getReceiver() {
		return receiver;
	}
	
	public String getSender() {
		return sender;
	}
	
	
	
	
}
