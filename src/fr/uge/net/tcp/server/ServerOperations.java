package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.LoginResponse;
import fr.uge.net.tcp.server.replies.PublicMessageResponse;
import fr.uge.net.tcp.server.replies.Response;
import fr.uge.net.tcp.server.replies.Response.Codes;


class ServerOperations {

	private final HashMap<String, SocketChannel> clients;
	
	ServerOperations(){
		this.clients = new HashMap<String, SocketChannel>();
	}
	

	Response regesterLogin(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		System.out.println("------------");
		System.out.println("clinet login "+login);
		for (String  key : clients.keySet()) {
			System.out.println("login "+ key+" sc"+clients.get(key));
		}
		if(clients.containsKey(login)) {
			return new LoginResponse(Codes.LOGIN_REFUSED);
		}else {
			clients.put(login, sc);
			return new LoginResponse(Codes.LOGIN_ACCEPTED);
		}
	}
	
	boolean validUser(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);
		var clientChannel  = clients.get(login);
		try {
			return clientChannel != null &&
					clientChannel.getRemoteAddress().toString()
					.equals(sc.getRemoteAddress().toString());
		}catch(IOException e) {
			return false;
		}
	}
	/*
	Response makePublicMessage(String login, SocketChannel sc, String message) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(message);
		if(validUser(login, sc)) {
			return;
		}
		throw new IllegalArgumentException("Invalide login");
	}
	**/
}
