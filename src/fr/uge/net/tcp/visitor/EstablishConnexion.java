package fr.uge.net.tcp.visitor;

public class EstablishConnexion implements Frame{

	private final Long id;
	
	public EstablishConnexion(Long id) {
		System.out.println("id "+id );
		this.id = id;
	}
	
	/**
	 * Accept method for visiting the EstablishConnexion object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the EstablishConnexion object
	 * */
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
	}
	
	/**
	 * Gets connect_id from a private connection established
	 * 
	 * @return the id
	 * */	
	public Long getId() {
		return id;
	}

}
