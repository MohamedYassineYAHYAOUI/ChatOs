package fr.uge.net.tcp.visitor;


public interface FrameVisitor {

	
	public void visit(PublicMessage publicMessage);

	public void visit(ConnectToServer connectToServer);

	public void visit(PrivateMessage privateMessage);
	
	public void visit(PrivateConnexionRequest privateMessage);

	public void visit(PrivateConnexionRefused privateConnexionRefused);

	public void visit(PrivateConnexionAccepted privateConnexionAccepted);

	public void visit(ServerConnection serverConnection);

	public void visit(EstablishConnexion establishConnexion);
	
	public void visit(DisconnectFromServer disconnectFromServer);

}
