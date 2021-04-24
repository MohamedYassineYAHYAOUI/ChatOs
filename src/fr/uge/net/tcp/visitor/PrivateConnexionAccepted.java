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
	
	/**
	 * Accept method for visiting the PrivateConnexionAccepted object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the PrivateConnexion object
	 * */
	public void accept(FrameVisitor frameVisitor){
		frameVisitor.visit(this);
	}
	
	/**
	 * Gets the connect id from the private connection
	 * 
	 * @return the id
	 * @throws IllegalStateException if the request don't have id
	 * */
	public Long getId() {
		if( id == null) {
			throw new IllegalStateException("request with no id");
		}
		return id;
	}

}
