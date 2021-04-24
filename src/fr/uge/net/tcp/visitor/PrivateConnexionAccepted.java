package fr.uge.net.tcp.visitor;


public class PrivateConnexionAccepted extends AbstractCommonMessage implements Frame{

	private Long id;
	
	public PrivateConnexionAccepted(String sender, String receiver, Long id) {
		super(sender, receiver);
		this.id = id;
	}
	
	public PrivateConnexionAccepted(String sender, String receiver){
		super(sender, receiver);
		id = null;
	}
	
	public void accept(FrameVisitor frameVisitor){
		frameVisitor.visit(this);
	}

	public Long getId() {
		if( id == null) {
			throw new IllegalStateException("request with no id");
		}
		return id;
	}

}
