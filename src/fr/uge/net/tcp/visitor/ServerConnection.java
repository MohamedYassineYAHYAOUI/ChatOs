package fr.uge.net.tcp.visitor;

import java.util.Objects;


public class ServerConnection implements Frame{
	
	private final String login;
	
	public ServerConnection(String login){
		this.login = Objects.requireNonNull(login);
	}
	
	/**
	 * Accept method for visiting the ServerConnection object
	 * 
	 * @param frameVisitor the FrameVisitor for visiting the ServerConnection object
	 * */
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
		
	}
	
	/**
	 * Gets the client login which connects to the server
	 * 
	 * @return the client login
	 * */
	public String getLogin() {
		return login;
	}
	
	
	
	
	
}
