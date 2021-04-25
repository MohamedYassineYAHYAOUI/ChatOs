package fr.uge.net.tcp.visitor;

public  interface Frame{
	
	/**
	* Accept method for the Frame Visitor
	* 
	* @param frameVisitor the frame visitor to accept
	**/
	public void accept(FrameVisitor frameVisitor);
}
