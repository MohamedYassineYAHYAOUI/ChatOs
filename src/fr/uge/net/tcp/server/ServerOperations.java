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

	// private final HashMap<String, SocketChannel> clients;
	private final HashMap<SocketChannel, String> clients;

	ServerOperations(){
		this.clients = new HashMap<SocketChannel,String >();
	}

	Response regesterLogin(String login, SocketChannel sc) {
		Objects.requireNonNull(login);

		if (clients.containsKey(sc)) {
			return new LoginResponse(Codes.LOGIN_REFUSED);
		} else {
			clients.put(sc, login);
			return new LoginResponse(Codes.LOGIN_ACCEPTED);
		}
	}

	void removeClient(SocketChannel sc) {
		Objects.requireNonNull(sc);
		clients.remove(sc);
	}
	
	boolean validUser(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);
		var clientLogin = clients.get(sc);
//		try {
			return clientLogin != null
					&& login.equals(clientLogin);
					//&& clientChannel.getRemoteAddress().toString().equals(sc.getRemoteAddress().toString());
//		} catch (IOException e) {
//			return false;
//		}
	}
}
