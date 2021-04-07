package fr.uge.net.tcp.client;


import java.util.AbstractMap.SimpleEntry;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.MessageResponse;

/**
 * Thread Safe
 */
class PrivateConnectionTraitement {

	static private Logger logger = Logger.getLogger(PrivateConnectionTraitement.class.getName());
	//private final HashMap<String, Boolean> requestHistory = new HashMap<>();
	final private HashMap<String, SimpleEntry<Long, Context>> clientsWithPrivateConnexion = new HashMap<>();
	// final private InetSocketAddress isa;
	private final MessageResponse.Builder packetBuilder = new MessageResponse.Builder();
	// private final Consumer<ByteBuffer> consumer;

	boolean hasPrivateConnexion(String targetClientLogin) {
		Objects.requireNonNull(targetClientLogin);
		return clientsWithPrivateConnexion.containsKey(targetClientLogin);
	}
/*
	boolean requesteForPrivateConnexionInProgress(String target) {
		var sentRequest = requestHistory.get(target);
		return sentRequest != null;
	}

	void addToHistory(String targetLogin) {
		Objects.requireNonNull(targetLogin);
		requestHistory.put(targetLogin, false);
	}
	*/


	/*
	 * boolean requestWasTreated(String targetLogin) {
	 * Objects.requireNonNull(targetLogin); synchronized (requestHistory) { var
	 * value = requestHistory.get(targetLogin); return value != null && value ==
	 * true; } }
	 * 
	 * void createPrivateConnection(String clientTarget, long id ) { synchronized
	 * (requestHistory) { try { var sc = SocketChannel.open();
	 * //sc.configureBlocking(false); sc.connect(isa);
	 * clientsWithPrivateConnexion.put(clientTarget, new SimpleEntry<>(id, sc));
	 * System.out.println("connected with server with "+clientTarget);
	 * 
	 * }catch(IOException e) { logger.log(Level.WARNING,
	 * "private connexion socket failed to connect to server"); return; } } }
	 * 
	 * void addPrivateConnexion(String login, Long connect_id) throws IOException {
	 * Objects.requireNonNull(login);
	 * 
	 * if (!clientsWithPrivateConnexion.containsKey(login)) { var sc =
	 * SocketChannel.open(); sc.connect(isa); clientsWithPrivateConnexion.put(login,
	 * new SimpleEntry<Long, SocketChannel>(connect_id, sc)); } throw new
	 * IllegalStateException("connexion already established");
	 * 
	 * }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * void wakeUpConsumers(String target) { synchronized (requestHistory) {
	 * requestHistory.put(target, true); requestHistory.notifyAll(); } }
	 * 
	 * 
	 * 
	 * 
	 * void sendMessageViaPrivateConnection(String login, String target, String
	 * message) { Objects.requireNonNull(message); Objects.requireNonNull(login);
	 * Objects.requireNonNull(target);
	 * 
	 * new Thread(() -> {
	 * 
	 * try { synchronized (requestHistory) {
	 * 
	 * if(!hasPrivateConnexion(target)) {
	 * 
	 * var sentRequest = requestHistory.get(target);
	 * 
	 * while(sentRequest == null || sentRequest== false) { requestHistory.wait();
	 * sentRequest = requestHistory.get(target); } var pcWithTarget =
	 * clientsWithPrivateConnexion.get(target); if(pcWithTarget == null) {
	 * System.out.println("connexion réfusé "+target); return; }
	 * //packetBuilder.setPacketCode(Codes.LOGIN_PRIVATE).setId(pcWithTarget.getKey(
	 * )); //consumer.accept(packetBuilder.build().getResponseBuffer());
	 * //System.out.println("send LOGIN_PRIVATE(9) from if"); //envoie de
	 * LOGIN_PRIVATE(9) = 9 (OPCODE) connect_id (LONG) (sc)
	 * 
	 * } var pcWithTarget = clientsWithPrivateConnexion.get(target);
	 * packetBuilder.setPacketCode(Codes.LOGIN_PRIVATE).setId(pcWithTarget.getKey())
	 * ; consumer.accept(packetBuilder.build().getResponseBuffer());
	 * System.out.println("send LOGIN_PRIVATE(9) from else"); //envoie de
	 * LOGIN_PRIVATE(9) = 9 (OPCODE) connect_id (LONG) (sc) } return;
	 * }catch(InterruptedException e) { logger.log(Level.WARNING,
	 * "thread message sender interrupted "+e.getMessage()); return ;
	 * }catch(NullPointerException e) { logger.log(Level.WARNING,
	 * "thread message sender interrupted, null pointer "+e.getMessage()); return ;
	 * }
	 * 
	 * }).start();
	 * 
	 * }
	 */

}
