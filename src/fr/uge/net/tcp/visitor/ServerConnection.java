package fr.uge.net.tcp.visitor;

import java.util.Objects;

public class ServerConnection implements Frame{
	
	private final String login;
	
	public ServerConnection(String login){
		this.login = Objects.requireNonNull(login);
	}
	
	
	@Override
	public void accept(FrameVisitor frameVisitor) {
		frameVisitor.visit(this);
		
	}
	
	
	public String getLogin() {
		return login;
	}
	
	
	
	
	
}
