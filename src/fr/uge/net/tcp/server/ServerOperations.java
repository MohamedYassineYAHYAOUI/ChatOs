package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.LoginResponse;
import fr.uge.net.tcp.server.replies.PublicMessageResponse;
import fr.uge.net.tcp.server.replies.Response;
import fr.uge.net.tcp.server.replies.Response.ResponseCodes;


class ServerOperations {

	private final HashMap<String, SocketChannel> clients;
	
	ServerOperations(){
		this.clients = new HashMap<String, SocketChannel>();
	}
	

	Response regesterLogin(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		if(clients.containsKey(login)) {
			return new LoginResponse(ResponseCodes.LOGIN_REFUSED);
		}else {
			clients.put(login, sc);
			return new LoginResponse(ResponseCodes.LOGIN_ACCEPTED);
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
