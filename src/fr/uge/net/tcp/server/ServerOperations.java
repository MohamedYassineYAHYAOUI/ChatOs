package fr.uge.net.tcp.server;


import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.Response.Codes;

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

	
	static private final Logger logger = Logger.getLogger(ServerOperations.class.getName());
	private final HashMap<SocketChannel, ClientConnexions >clients;
	private final HashMap<Long, SimpleEntry<PrivateConnexionSocket, PrivateConnexionSocket>> currentPrivateConnexions;
	

	ServerOperations() {
		this.clients = new HashMap<SocketChannel, ClientConnexions>();
		this.currentPrivateConnexions = new HashMap<Long, SimpleEntry<PrivateConnexionSocket, PrivateConnexionSocket>>();
	}

	Codes regesterLogin(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);

		for(var connexion : clients.values()) {
			if(connexion.login.equals(login)) {
				return Codes.LOGIN_REFUSED;
			}
		}
		clients.put(sc, new ClientConnexions(login));
		return Codes.LOGIN_ACCEPTED;
	}

	/**
	 * Registers the private connection between the requester and the target with their connect id
	 * 
	 * @param connectId the connect id of the private connection
	 * @param requesterSC the socket channel of the requester client
	 * @param targetSC the socket channel of the target client
	 */
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
	
	/**
	 * Removes a client with his id gived in parameter
	 * 
	 * @param id
	 */
	void removeClient(long id) {
		var pc = currentPrivateConnexions.get(id);
	
		if(pc==null) {
			return;
		}
		if(pc.getKey().context != null) {
			pc.getKey().context.silentlyClose();	
		}
		if(pc.getValue().context != null) {
			pc.getValue().context.silentlyClose();
		}
		currentPrivateConnexions.remove(id);

	}
	
	/**
	 * Removes a client with his socket channel gived in parameter
	 * 
	 * @param sc
	 */
	void removeClient(SocketChannel sc) {

		Objects.requireNonNull(sc);
		var connexionClient = clients.get(sc);
		if(connexionClient == null) {
			return;
		}
		for(var connexionId : connexionClient.connexionsIds) {
			removeClient(connexionId);
		}
		clients.remove(sc);
	}
	
	/**
	 * Checks if the client connected is same than the login gives in parameter
	 * using the socket channel
	 * 
	 * @param login
	 * @param sc
	 * @return true if it's the same and false in the other cases
	 */
	boolean validUser(String login, SocketChannel sc) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(sc);
		var clientConnexion = clients.get(sc);

		return clientConnexion != null && clientConnexion.login.equals(login);
	}
	

	/**
	 * Checks if the connection is establish
	 * 
	 * @param context
	 * @param connectId
	 * @return true if the connection is establish and false in the other case
	 */
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
	
	/**
	 * Gets the context of two clients using the connect id
	 * 
	 * @param connectId
	 * @return the contexts of this clients
	 */
	SimpleEntry<Context, Context> getClientsContext(long connectId){
		var pc =  currentPrivateConnexions.get(connectId);
		if(pc == null) {
			throw new IllegalArgumentException("id not valid");
		}
		return new SimpleEntry<>(pc.getKey().context,pc.getValue().context);
	}
	

	
}
