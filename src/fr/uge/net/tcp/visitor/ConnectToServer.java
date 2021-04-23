package fr.uge.net.tcp.visitor;

public class ConnectToServer extends Frame {

	private final boolean flag;
	
	public ConnectToServer(boolean flag) {
		this.flag = flag;
	}

	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}

	public boolean isConnected() {
		return flag;
	}
	
	
	
	
	
}
