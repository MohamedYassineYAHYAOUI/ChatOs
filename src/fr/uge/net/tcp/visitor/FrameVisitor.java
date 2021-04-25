package fr.uge.net.tcp.visitor;


public interface FrameVisitor {

	/**
	 * Visit method for the PublicMessage object
	 * 
	 * @param publicMessage the PublicMessage object to visit
	 * */
	public void visit(PublicMessage publicMessage);

	/**
	 * Visit method for the ConnectToServer object
	 * 
	 * @param connectToServer the ConnectToServer object to visit
	 * */
	public void visit(ConnectToServer connectToServer);

	/**
	 * Visit method for the PrivateMessage object
	 * 
	 * @param privateMessage the PrivateMessage object to visit
	 * */
	public void visit(PrivateMessage privateMessage);
	
	/**
	 * Visit method for the PrivateConnexionRequest object
	 * 
	 * @param privateMessage the PrivateConnexionRequest object to visit
	 * */
	public void visit(PrivateConnexionRequest privateMessage);

	/**
	 * Visit method for the PrivateConnexionRefused object
	 * 
	 * @param privateConnexionRefused the PrivateConnexionRefused object to visit
	 * */
	public void visit(PrivateConnexionRefused privateConnexionRefused);

	/**
	 * Visit method for the PrivateConnexionAccepted object
	 * 
	 * @param privateConnexionAccepted the PrivateConnexionAccepted object to visit
	 * */
	public void visit(PrivateConnexionAccepted privateConnexionAccepted);

	/**
	 * Visit method for the ServerConnection object
	 * 
	 * @param serverConnection the ServerConnection object to visit
	 * */
	public void visit(ServerConnection serverConnection);

	/**
	 * Visit method for the EstablishConnexion object
	 * 
	 * @param establishConnexion the EstablishConnexion object to visit
	 * */
	public void visit(EstablishConnexion establishConnexion);
	
	/**
	 * Visit method for the DisconnectFromServer object
	 * 
	 * @param disconnectFromServer the DisconnectFromServer object to visit
	 * */
	public void visit(DisconnectFromServer disconnectFromServer);

}
