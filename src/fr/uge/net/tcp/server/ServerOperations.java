package fr.uge.net.tcp.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.LoginResponse;
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
	
	
}
