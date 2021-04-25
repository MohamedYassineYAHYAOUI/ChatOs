package fr.uge.net.tcp.visitor;


/**
 * 
 */
public class PrivateConnexionRefused extends AbstractCommonMessage implements Frame{

	public PrivateConnexionRefused( String sender, String receiver){
		super(sender, receiver);
	}
	
	/**
	 * Accept method for visiting the PrivateConnexionRefused object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the PrivateConnexionRefused object
	 * */
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
}
