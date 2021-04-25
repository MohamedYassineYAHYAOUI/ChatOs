package fr.uge.net.tcp.visitor;

public class DisconnectFromServer implements Frame{


	private final Long id;
	
	public DisconnectFromServer(Long id) {
		this.id = id;
	}
	
	/**
	 * Accept method for visiting the DisconnectFromServer object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the DisconnectFromServer object
	 * */
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
	
	/**
	 * Gets connect_id from a private connection disconnected
	 * 
	 * @return the id
	 * */
	public Long getId() {
		return id;
	}


}
