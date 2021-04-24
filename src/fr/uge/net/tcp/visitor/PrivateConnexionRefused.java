package fr.uge.net.tcp.visitor;

public class PrivateConnexionRefused  extends AbstractCommonMessage implements Frame{

	public PrivateConnexionRefused( String sender, String receiver){
		super(sender, receiver);
	}
	
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
}
