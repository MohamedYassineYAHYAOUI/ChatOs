package fr.uge.net.tcp.visitor;

public class DisconnectFromServer implements Frame{


	private final Long id;
	
	public DisconnectFromServer(Long id) {
		this.id = id;
	}
	
	
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
	
	public Long getId() {
		return id;
	}


}
