package fr.uge.net.tcp.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;

import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;

class Server {

	static private Logger logger = Logger.getLogger(Server.class.getName());

	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;
	private final ServerOperations serverOperations;
	private final MessageResponse.Builder Packetbuilder = new MessageResponse.Builder();

	private final Random random = new Random();

	public Server(int port) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverOperations = new ServerOperations();
	}

	/**
	 * Register a client in the server
	 * 
	 * @param login the login of the client
	 * @param key the context of the client
	 */
	void registerLogin(String login, SelectionKey key) {
		var context = (Context) key.attachment();
		var codeResponse = serverOperations.regesterLogin(login, (SocketChannel) key.channel());
		context.queueResponse(Packetbuilder.setPacketCode(codeResponse).build());
	}

	/**
	 * Send a message from a client to all the other clients except himself
	 * 
	 * @param login the login of the client which sends the message
	 * @param message the message to send
	 * @param key the context of the client which sends the message
	 */
	void broadcast(String login, String message, SelectionKey key) {
		var sc = (SocketChannel) key.channel();

		if (serverOperations.validUser(login, sc)) {
			var senderContext = (Context) key.attachment();
			for (var clientKey : selector.keys()) {
				var context = (Context) clientKey.attachment();
				if (context != null && !context.equals(senderContext) && context.isMainChannel()) {
					Packetbuilder.setPacketCode(Codes.PUBLIC_MESSAGE_RECEIVED).setLogin(login).setMessage(message);
					context.queueResponse(Packetbuilder.build());
				}
			}
		} else {
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}

	/**
	 * Removes a client with his key
	 * @param key
	 */
	void removeClient(SelectionKey key) {
		var sc = (SocketChannel) key.channel();
		serverOperations.removeClient(sc);
	}

	/**
	 * Removes a client with his id
	 * @param id
	 */
	void removeClient(long id) {
		serverOperations.removeClient(id);
	}
	
	/**
	 * Establishes a private connection using the connect id and the key
	 * @param connectId
	 * @param key
	 */
	void establishConnection(long connectId, SelectionKey key) {
		Objects.requireNonNull(key);
		
		System.out.println("login private "+connectId);
		var context = (Context) key.attachment();
		if (serverOperations.establishConnection(context, connectId)) {
			var clientsChannels = serverOperations.getClientsContext(connectId);
			Packetbuilder.setPacketCode(Codes.ESTABLISHED);
			System.out.println("SET CONNECTIOn");
			
			clientsChannels.getKey().setPrivateConnection(clientsChannels.getValue());
			clientsChannels.getKey().queueResponse(Packetbuilder.build());
			
			Packetbuilder.setPacketCode(Codes.ESTABLISHED);
			clientsChannels.getValue().setPrivateConnection(clientsChannels.getKey());
			clientsChannels.getValue().queueResponse(Packetbuilder.build());
		}
	}

	/**
	 * Redirects the private connexion request from the requester client to the target client or vice-versa
	 * 
	 * @param login_requester the login of the requester client
	 * @param login_target the login of the target client
	 * @param key the context of the requester client
	 * @param codePacket the code for the type of the packet
	 * @param reverse a boolean if we want to redirect the pack from the target client to the requester client
	 */
	void redirectPrivateConnexionRequest(String login_requester, String login_target, SelectionKey key,
			Codes codePacket, boolean reverse) {
		var sc = (SocketChannel) key.channel();

		if (!serverOperations.validUser(reverse ? login_target: login_requester, sc)) {
			logger.log(Level.INFO, "ignored invalide request from Client");
			return;
		}
		for (var clientKey : selector.keys()) {
			var context = (Context) clientKey.attachment();
			if (context == null) {
				continue;
			}
			var scTarget = (SocketChannel) clientKey.channel();
			if (serverOperations.validUser(reverse ? login_requester : login_target , scTarget)
					&& context.isMainChannel()) {

				Packetbuilder.setPacketCode(codePacket).setLogin(login_requester)
				.setTargetLogin(login_target); // buffe builder

				context.queueResponse(Packetbuilder.build());
				return;
			}
		}
	}

	/**
	 * Redirects the connect id to the requester client and the target client
	 * 
	 * @param login_requester the login of the requester client
	 * @param login_target the login of the target client
	 * @param key the context
	 */
	void redirectIdPacket(String login_requester, String login_target, SelectionKey key) {
		var target_sc = (SocketChannel) key.channel();
		var target_context = (Context) key.attachment();
		if (!serverOperations.validUser(login_target, target_sc)) {
			logger.log(Level.INFO, "ignored invalide request from Client");
			return;
		}
		for (var clientKey : selector.keys()) {
			var sender_Context = (Context) clientKey.attachment();
			if (sender_Context == null) {
				continue;
			}
			var sender_sc = (SocketChannel) clientKey.channel();

			if (serverOperations.validUser(login_requester, sender_sc) && target_context.isMainChannel()
					&& sender_Context.isMainChannel()) {

				var idCode = random.nextLong();

				serverOperations.registerPrivateConnection(idCode, sender_sc, target_sc);

				Packetbuilder.setPacketCode(Codes.ID_PRIVATE).setLogin(login_requester).setTargetLogin(login_target)
						.setId(idCode); // buffer builder
				target_context.queueResponse(Packetbuilder.build());
				Packetbuilder.setPacketCode(Codes.ID_PRIVATE).setLogin(login_requester).setTargetLogin(login_target)
						.setId(idCode); // buffer builder
				sender_Context.queueResponse(Packetbuilder.build());

				return;
			}
		}

	}
	/**
	 * Send a private message in a private connexion from the sender client to the target client
	 * 
	 * @param senderLogin the sender login
	 * @param targetLogin the target login
	 * @param message the message to send
	 * @param key the context
	 */
	void sendPrivateMessage(String senderLogin, String targetLogin, String message, SelectionKey key) {
		var sc = (SocketChannel) key.channel();

		if (serverOperations.validUser(senderLogin, sc)) {
			for (var clientKey : selector.keys()) {
				var context = (Context) clientKey.attachment();
				if (context != null) {
					var scTarget = (SocketChannel) clientKey.channel();
					if (serverOperations.validUser(targetLogin, scTarget) && context.isMainChannel()) {

						Packetbuilder.setPacketCode(Codes.PRIVATE_MESSAGE_RECEIVED).setLogin(senderLogin).setTargetLogin(targetLogin)
								.setMessage(message);
						context.queueResponse(Packetbuilder.build());
						return;
					}
				}
			}
		} else {
			logger.log(Level.INFO, "ignored invalide request from Client");
		}
	}

	/**
	 * Accept a connection
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void doAccept(SelectionKey key) throws IOException {
		var ssc = (ServerSocketChannel) key.channel();
		var sc = ssc.accept();

		if (sc != null) {
			sc.configureBlocking(false);
			var scKey = sc.register(selector, SelectionKey.OP_READ);
			scKey.attach(new Context(this, scKey));
		}
	}

	/**
	 * Launch the server
	 * 
	 * @throws IOException
	 */
	void launch() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (!Thread.interrupted()) {
			printKeys(); // for debug
			System.out.println("Starting select");
			try {
				selector.select(this::treatKey);
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
			System.out.println("Select finished");
		}
	}

	/**
	 * Treat the state of the key gives in parameter
	 * 
	 * @param key
	 */
	private void treatKey(SelectionKey key) {
		printSelectedKey(key); // for debug
		try {
			if (key.isValid() && key.isAcceptable()) {
				doAccept(key);
			}
		} catch (IOException ioe) {
			// lambda call in select requires to tunnel IOException
			throw new UncheckedIOException(ioe);
		}
		try {
			if (key.isValid() && key.isWritable()) {
				((Context) key.attachment()).doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				((Context) key.attachment()).doRead();
			}
		} catch (IOException e) {
			logger.log(Level.INFO, "Connection closed with client due to IOException", e);
			silentlyClose(key);
		}
	}

	/**
	 * Close a client
	 * 
	 * @param key
	 */
	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();
		try {
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

	/***
	 * Theses methods are here to help understanding the behavior of the selector
	 ***/

	private String interestOpsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		int interestOps = key.interestOps();
		ArrayList<String> list = new ArrayList<>();
		if ((interestOps & SelectionKey.OP_ACCEPT) != 0)
			list.add("OP_ACCEPT");
		if ((interestOps & SelectionKey.OP_READ) != 0)
			list.add("OP_READ");
		if ((interestOps & SelectionKey.OP_WRITE) != 0)
			list.add("OP_WRITE");
		return String.join("|", list);
	}

	/**
	 * Prints keys
	 */
	public void printKeys() {
		Set<SelectionKey> selectionKeySet = selector.keys();
		if (selectionKeySet.isEmpty()) {
			System.out.println("The selector contains no key : this should not happen!");
			return;
		}
		System.out.println("The selector contains:");
		for (SelectionKey key : selectionKeySet) {
			SelectableChannel channel = key.channel();
			if (channel instanceof ServerSocketChannel) {
				System.out.println("\tKey for ServerSocketChannel : " + interestOpsToString(key));
			} else {
				SocketChannel sc = (SocketChannel) channel;
				System.out.println("\tKey for Client " + remoteAddressToString(sc) + " : " + interestOpsToString(key));
			}
		}
	}

	/**
	 * Remote address of the socket channel gived in parameter
	 * @param sc
	 * @return the remote address
	 * @return "???" if the IOException is catch
	 */
	private String remoteAddressToString(SocketChannel sc) {
		try {
			return sc.getRemoteAddress().toString();
		} catch (IOException e) {
			return "???";
		}
	}

	/**
	 * Prints the key gived in parameter
	 * 
	 * @param key
	 */
	public void printSelectedKey(SelectionKey key) {
		SelectableChannel channel = key.channel();
		if (channel instanceof ServerSocketChannel) {
			System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
		} else {
			SocketChannel sc = (SocketChannel) channel;
			System.out.println(
					"\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
		}
	}

	/**
	 * Converts into string the possible actions of the key gived in parameter
	 * @param key
	 * @return the possible actions
	 */
	private String possibleActionsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		ArrayList<String> list = new ArrayList<>();
		if (key.isAcceptable())
			list.add("ACCEPT");
		if (key.isReadable())
			list.add("READ");
		if (key.isWritable())
			list.add("WRITE");
		return String.join(" and ", list);
	}
}
