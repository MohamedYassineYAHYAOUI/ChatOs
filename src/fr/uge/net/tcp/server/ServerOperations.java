package fr.uge.net.tcp.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;


import fr.uge.net.tcp.server.replies.LoginResponse;
import fr.uge.net.tcp.server.replies.Response;
import fr.uge.net.tcp.server.replies.Response.Codes;

class ServerOperations {

	private class ClientConnexions{
		private final String login;
		private final HashSet<Long> connexionsIds;
		
		ClientConnexions(String login){
			this.login = login;
			this.connexionsIds = new HashSet<>();
		}
	}

	private class PrivateConnexionSocket{
		private Context context ; // Can be Null
		private boolean connected; // false
		
		
		void setContext(Context context) {
			if(this.context != null) {
				throw new IllegalStateException("context exist already");
			}
			this.context = Objects.requireNonNull(context);
			connected = true;	
		}
		
	}
	//ID_PRIVATE(8) = 8 (OPCODE) login_requester (STRING) login_target (STRING) connect_id (LONG)
	//LOGIN_PRIVATE(9) = 9 (OPCODE) connect_id (LONG)

	
	static private final Logger logger = Logger.getLogger(ServerOperations.class.getName());
	private final HashMap<SocketChannel, ClientConnexions >clients;
	private final HashMap<Long, SimpleEntry<PrivateConnexionSocket, PrivateConnexionSocket>> currentPrivateConnexions;
	
	//<SocketChannel, String, list<id> > clients;
	//id  <<sc1,true>   <sc2,true> >  

	ServerOperations() {
		this.clients = new HashMap<SocketChannel, ClientConnexions>();
		this.currentPrivateConnexions = new HashMap<Long, SimpleEntry<PrivateConnexionSocket, PrivateConnexionSocket>>();
	}

	Response regesterLogin(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);

		for(var connexion : clients.values()) {
			if(connexion.login.equals(login)) {
				return new LoginResponse(Codes.LOGIN_REFUSED);
			}
		}
		clients.put(sc, new ClientConnexions(login));
		return new LoginResponse(Codes.LOGIN_ACCEPTED);
	}

	void registerPrivateConnection(long connectId, SocketChannel requesterSC, SocketChannel targetSC) {
		var requesterConnection = clients.get(requesterSC);
		var targetConnection = clients.get(targetSC);
		if(requesterConnection == null || targetConnection == null) {
			throw new IllegalStateException("SC in null");
		}
		requesterConnection.connexionsIds.add(connectId);
		targetConnection.connexionsIds.add(connectId);
		currentPrivateConnexions.put(connectId, 
				new SimpleEntry<>(new PrivateConnexionSocket() , new PrivateConnexionSocket() ) );
	}
	
	void removeClient(SocketChannel sc) {
		Objects.requireNonNull(sc);
		var connexionClient = clients.get(sc);
		if(connexionClient == null) {
			return;
		}
		for(var connexionId : connexionClient.connexionsIds) {
			var cp = currentPrivateConnexions.get(connexionId);
			if(cp != null) {
				if(cp.getKey().context != null) {
					cp.getKey().context.silentlyClose();	
				}
				if(cp.getValue().context != null) {
					cp.getValue().context.silentlyClose();
				}
			}
			currentPrivateConnexions.remove(connexionId);
		}
		clients.remove(sc);
	}
	
	boolean validUser(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);
		var clientConnexion = clients.get(sc);

		return clientConnexion != null && clientConnexion.login.equals(login);
	}
	

	boolean establishConnection(Context context, long connectId) {
		Objects.requireNonNull(context);
		var pc = currentPrivateConnexions.get(connectId);
		if(pc == null) {
			logger.info("request LOGIN PRIVATE ignored, due to unknown id");
			return false;
		}
		if(!pc.getKey().connected) {
			pc.getKey().setContext(context); 
		}else {
			pc.getValue().setContext(context);
		}
		return pc.getValue().connected && pc.getKey().connected;
	}
	
	SimpleEntry<Context, Context> getClientsContext(long connectId){
		var pc =  currentPrivateConnexions.get(connectId);
		if(pc == null) {
			throw new IllegalArgumentException("id not valid");
		}
		return new SimpleEntry<>(pc.getKey().context,pc.getValue().context);
	}
	
	
	
	
	
	
	
}
