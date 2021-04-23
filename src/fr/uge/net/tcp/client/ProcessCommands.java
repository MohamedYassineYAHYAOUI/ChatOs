package fr.uge.net.tcp.client;

import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;



import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;

class ProcessCommands {
	static private Logger logger = Logger.getLogger(ClientOS.class.getName());
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	private final String login;
	private final ClientOS client;
	private final MessageResponse.Builder packetBuilder;
	
	
	ProcessCommands(String userLogin, ClientOS clientOs  ){
		this.login = Objects.requireNonNull(userLogin);
		this.client = Objects.requireNonNull(clientOs);
		this.packetBuilder = new MessageResponse.Builder();

	}
	
	boolean queueIsEmpty() {
		synchronized (commandQueue) {
			return commandQueue.isEmpty();
		}
	}
	
	void addToQueue(String message) {
		Objects.requireNonNull(message);
		synchronized (commandQueue) {
			commandQueue.add(message);
		}
	}
	
	private String privateMessage(String msg) {
		//@Bob
		var targetLogin = msg.split(" ")[0].substring(1);
		synchronized (commandQueue) {
			if(targetLogin.equals(login)) {
				throw new IllegalArgumentException("Can't send private message to self ");
			}
			packetBuilder.setPacketCode(Codes.PRIVATE_MESSAGE_SENT).setLogin(login).setTargetLogin(targetLogin)
			.setMessage(msg.substring(targetLogin.length() + 2)); // buffer builder
		}
		return targetLogin;

	}
	private void publicMessage(String msg) {
		synchronized (commandQueue) {
			packetBuilder.setPacketCode(Codes.PUBLIC_MESSAGE_SENT).setLogin(login).setMessage(msg);
		}
	}
	
	String nextMessage() {
		synchronized (commandQueue) {
			return commandQueue.poll();
		}	
	}
	
	private String privateConnexion(String msg) {
		var tmp = msg.split(" ");
		var targetLogin = tmp[0].substring(1); // target Login
		if (targetLogin.equals(login)) { // if request to the client himself, ignore
			throw new IllegalArgumentException("Can't start private connexion with self ");
		}
		//var pc = privateConnexion.get(targetLogin);
		var pc = client.getPrivateConnexion(targetLogin);
		if (tmp.length == 1 && pc != null) { //disconnect from a private connection
			if (pc.getKey() != null) { // the connection was established (not pending)
				
				packetBuilder.setPacketCode(Codes.DISCONNECT_PRIVATE).setId(pc.getKey().getId());
			}
			client.removePrivateConnection(targetLogin); 
			return null;
		}
		String message = msg.substring(targetLogin.length() + 2); //client inital message

		if (pc == null){ // no request for the target
			packetBuilder.setPacketCode(Codes.REQUEST_PRIVATE_CONNEXION).setLogin(login)
					.setTargetLogin(targetLogin); // buffer builder
			client.addPrivateConnexion(targetLogin, new SimpleEntry<PrivateContext, String>(null, message)); //store the initial message
		} else {
			if (pc.getKey() != null) { // connection established
				pc.getKey().queueMessage(packetBuilder.setMessage(message).build().getResponseBuffer());
			}
			return null;
		}
		return targetLogin;
	}
	
	void processUserInput() {
		while (!commandQueue.isEmpty()) {
			try {
				var msg = nextMessage();
				if(msg == null) {
					break;
				}
				String targetLogin = null;
				if(msg.startsWith("/")) {
					targetLogin  = privateConnexion(msg);
				}else if(msg.startsWith("@")) {
					targetLogin = privateMessage(msg);
				}else {
					publicMessage(msg);
				}
				if (targetLogin != null && targetLogin.equals(login)) {
					continue;
				}
				client.queueMessage(packetBuilder.build().getResponseBuffer()); // queue message 

			} catch (StringIndexOutOfBoundsException e) {
				logger.info("invalide request, ignored");
			} catch (IllegalArgumentException e) {
				logger.info(e.getMessage());
			}finally {
				packetBuilder.resetBuilder();
			}
			
		}
	}

	
	
	
	
}
