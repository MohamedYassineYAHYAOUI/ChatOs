package fr.uge.net.tcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Objects;

import fr.uge.net.tcp.server.replies.MessageResponse;
import fr.uge.net.tcp.server.replies.Response.Codes;

/**
 * Thread Safe
 */
class PrivateConnectionTraitement {

	static private Logger logger = Logger.getLogger(PrivateConnectionTraitement.class.getName());
	private final HashMap<String, Boolean> requestHistory = new HashMap<>();
	final private HashMap<String, SimpleEntry<Long, SocketChannel>> clientsWithPrivateConnexion = new HashMap<>();
	final private InetSocketAddress isa;
	private  MessageResponse.Builder packetBuilder;
	private final Consumer<ByteBuffer> consumer;

	PrivateConnectionTraitement(InetSocketAddress isa, Consumer<ByteBuffer> consumer) {
		this.isa = Objects.requireNonNull(isa);
		this.consumer = Objects.requireNonNull(consumer); 
	}

	void addToHistory(String targetLogin) {
		Objects.requireNonNull(targetLogin);
		synchronized (requestHistory) {
			requestHistory.put(targetLogin, false);
		}
	}

	boolean requestWasTreated(String targetLogin) {
		Objects.requireNonNull(targetLogin);
		synchronized (requestHistory) {
			var value = requestHistory.get(targetLogin);
			return value != null && value == true;
		}
	}

	private SocketChannel createPrivateConnection() throws IOException {

		// sc.configureBlocking(false);
		sc.connect(isa);
		return sc;
	}

	void addPrivateConnexion(String login, Long connect_id) throws IOException {
		Objects.requireNonNull(login);

		if (!clientsWithPrivateConnexion.containsKey(login)) {
			var sc = SocketChannel.open();
			sc.connect(isa);
			clientsWithPrivateConnexion.put(login, new SimpleEntry<Long, SocketChannel>(connect_id, sc));
		}
		throw new IllegalStateException("connexion already established");

	}

	private boolean hasPrivateConnexion(String targetClientLogin) {
		Objects.requireNonNull(targetClientLogin);
		return clientsWithPrivateConnexion.containsKey(targetClientLogin);
	}

	void sendMessageViaPrivateConnection(String login, String target, String message) {
		Objects.requireNonNull(message);
		Objects.requireNonNull(login);
		Objects.requireNonNull(target);

		new Thread(() -> {

			try {
				synchronized (requestHistory) {
					if(!hasPrivateConnexion(target)) {
						var sentRequest = requestHistory.get(target);
						if(sentRequest ==null){
							packetBuilder.setPacketCode(Codes.REQUEST_PRIVATE_CONNEXION).setLogin(login)
							.setTargetLogin(target); // buffer builder
							requestHistory.put(target, false);
						}
						//wait 
						//envoie de LOGIN_PRIVATE(9) = 9 (OPCODE) connect_id (LONG) (sc)

						
					}else {
						//envoie de LOGIN_PRIVATE(9) = 9 (OPCODE) connect_id (LONG) (sc)
					}
				}
				
			}catch(IOException e) {
				logger.warning("Thread private connection error "+e);
				return;
			}
			
		}).start();
		
	}

}
