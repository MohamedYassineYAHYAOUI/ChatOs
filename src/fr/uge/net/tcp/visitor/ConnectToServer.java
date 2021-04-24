package fr.uge.net.tcp.visitor;

public class ConnectToServer implements Frame {

	private final boolean flag;
	
	public ConnectToServer(boolean flag) {
		this.flag = flag;
	}

	/**
	 * Accept method for visiting the ConnectToServer object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the ConnectToServer object
	 **/
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
	
	/**
	 * Checks if a client is connected
	 * 
	 * @return the flag boolean
	 **/
	public boolean isConnected() {
		return flag;
	}
	
	
	
	
	
}
