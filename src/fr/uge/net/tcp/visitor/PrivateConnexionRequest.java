package fr.uge.net.tcp.visitor;


public class PrivateConnexionRequest extends AbstractCommonMessage  implements Frame {

	
	public PrivateConnexionRequest( String sender, String receiver){
		super(sender, receiver);
	}

	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
}
