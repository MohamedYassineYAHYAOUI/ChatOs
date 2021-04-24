package fr.uge.net.tcp.visitor;

public class EstablishConnexion implements Frame{

	private final Long id;
	
	public EstablishConnexion(Long id) {
		System.out.println("id "+id );
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
