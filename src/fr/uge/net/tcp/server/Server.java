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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.Response;


class Server {
	static private Logger logger = Logger.getLogger(Server.class.getName());

	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;
	private final ServerOperations serverOperations;

	public Server(int port) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverOperations = new ServerOperations();
	}
	
	
	/**
	 * Accept a connection from the server
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
			scKey.attach(new Context(this, scKey,serverOperations));
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
	 * Treat the state of the key,
	 * if the selectionkey is valide and can accepte a new connection, then connect
	 * check if the key can read or/ and write, if do call doread and/or dowrite
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
	 * close and release the key
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

	/**
	 * get the client socket channel with the name login
	 * @param login name of the client 
	 * @return SocketChannel of the client if the client exists, else null
	 */
	SocketChannel getClientSocketChannel(String login) {
		for (var clientKey : selector.keys()) {
			var context = (Context) clientKey.attachment();
			if (context != null) {
				var scTarget = (SocketChannel) clientKey.channel();
				if(serverOperations.validUser(login, scTarget)) {
					return scTarget;
				}
			}
		}
		return null;
	}
	
	/**
	 * Send a private  message response to the specific client loginReceiver if they exist
	 * @param loginReceiver the login of the client which receive the private message
	 * @param response the message to send to the client
	 */
	void sendPrivateMessage(String loginReceiver, Response response) {
		
		for (var clientKey : selector.keys()) {
			var context = (Context) clientKey.attachment();
			if (context != null) {
				var scTarget = (SocketChannel) clientKey.channel();
				System.out.println("loginReceiver" + loginReceiver);
				if(serverOperations.validUser(loginReceiver, scTarget) && context.isMainChannel()) {
					System.out.println("test "+loginReceiver);
					context.queueResponse(response);
					return;
				}
			}
		}
	}

	
/**
 * Send a message from a client  to all the other clients except himself
 * @param contextSender the context of the client sending the message
 * @param request the packet
 */
	void broadcast( Context contextSender, Response request ) {
		
		for (var clientKey : selector.keys()) {
			var context = (Context) clientKey.attachment();
			if (context != null && !context.equals(contextSender) && context.isMainChannel()) {				
				context.queueResponse(request);
			}
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
	public void printKeys(){
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
