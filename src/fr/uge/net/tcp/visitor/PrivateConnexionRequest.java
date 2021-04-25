package fr.uge.net.tcp.visitor;

public class PrivateConnexionRequest extends AbstractCommonMessage  implements Frame {

	
	public PrivateConnexionRequest( String sender, String receiver){
		super(sender, receiver);
	}

	/**
	 * Accept method for visiting the PrivateConnexionRequest object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the PrivateConnexionRequest object
	 * */	
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
}
