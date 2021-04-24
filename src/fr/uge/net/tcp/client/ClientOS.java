package fr.uge.net.tcp.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;

public class ClientOS {
	static private Logger logger = Logger.getLogger(ClientOS.class.getName());

	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private final String login;
	private final Thread console; // thread des input des commandes
	private final ProcessCommands inputProcess;

	private final HashMap<String, SimpleEntry<PrivateContext, String>> privateConnexion = new HashMap<>();

	private PublicContext uniqueContext;
	private final MessageResponse.Builder packetBuilder;
	
	/**
	 * constructor for clientOS 
	 * @param login client
	 * @param serverAddress server InetSocket 
	 * @throws IOException in case we can't open a new socketChannel
	 */
	public ClientOS(String login, InetSocketAddress serverAddress) throws IOException {
		this.serverAddress = Objects.requireNonNull(serverAddress);
		
		this.login = Objects.requireNonNull(login);
		this.packetBuilder = new MessageResponse.Builder();
		this.sc = SocketChannel.open();
		this.selector = Selector.open();
		this.console = new Thread(this::consoleRun);
		this.inputProcess = new ProcessCommands(login, this);
	}
	
	/**
	 * read the next line on the input and send it as a command to sendCommand
	 * if socketChannel is not open ask user to reconnect
	 * launch the connection to server process
	 */
	private void consoleRun() {
		try (var scan = new Scanner(System.in)) {
			processConnection();

			while (scan.hasNextLine()) {
				var msg = scan.nextLine();
				if (!sc.isOpen()) {
					System.out.println("please retry connecting to the server");
				} else {
					sendCommand(msg);
				}
			}
			return;
		} catch (InterruptedException e) {
			logger.info("Console thread has been interrupted");
			return;
		} finally {
			logger.info("Console thread stopping");
		}
	}
	
	
	/**
	 * Send a command to the selector via commandQueue and wake it up
	 * command must not be empty or blank
	 * @param msg
	 * @throws InterruptedException
	 */

	private void sendCommand(String msg) throws InterruptedException {
		Objects.requireNonNull(msg);
		try {
			synchronized (serverAddress) {
				if (!msg.isEmpty() && !msg.isBlank()) {
					//commandQueue.add(msg);
					inputProcess.addToQueue(msg);
				}
			}
			selector.wakeup();
		} catch (IllegalStateException e) {
			logger.warning("queue is full, msg ignored");
		}
	}
	
	/**
	 * build the packet asking to connect to server, and queue the packet in 
	 * the main context, then wake up the selector
	 */
	private void processConnection(){
		synchronized (serverAddress) {
			packetBuilder.setPacketCode(Codes.REQUEST_SERVER_CONNECTION).setLogin(login);
			uniqueContext.queueMessage(packetBuilder.build().getResponseBuffer());
		}
		selector.wakeup();
	}
	

	/**
	 * create private connection context for with the client targetlogin,
	 * and the unique code id
	 * @param targetLogin login of the other client in the private connection 
	 * @param id connection id
	 */
	void createPrivateConnection(String targetLogin, long id) {
		SocketChannel sc;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			var key = sc.register(selector, SelectionKey.OP_CONNECT);
			String initialMsg = null; // initial message /tragetLogin msg
			var pc = privateConnexion.get(targetLogin);
			if (pc != null) {
				initialMsg = pc.getValue(); 	//get registered message
			}
			synchronized (serverAddress) {
				sc.connect(serverAddress);
				var pccontext = PrivateContext.CreateContext(key, targetLogin, id, initialMsg); //create the private context
				key.attach(pccontext);
				privateConnexion.put(targetLogin, new SimpleEntry<PrivateContext, String>(pccontext, null)); 
			}
		} catch (IOException e) {
			logger.warning("error while creating private connection " + e.getMessage());
		}
	}
	
	/**
	 * Gets the private connection with another user gived in parameter
	 * 
	 * @param otherUser the other user
	 * @return the private connection
	 */
	SimpleEntry<PrivateContext, String> getPrivateConnexion(String otherUser){
		synchronized (serverAddress) {
			return privateConnexion.get(otherUser);
		}
	}
	
	/**
	 * Removes the private connection with the target client
	 * gived in parameter
	 * 
	 * @param targetLogin the target login of the client 
	 */
	void removePrivateConnection(String targetLogin) {
		synchronized (serverAddress) {
			privateConnexion.remove(targetLogin);
		}
	}

	/**
	 * Gets the client login
	 * 
	 * @return the login
	 */
	String getLogin() {
		return login;
	}
	
	/**
	 * Queue message for the packet
	 * 
	 * @param bb the packet
	 */
	void queueMessage(ByteBuffer bb) {
		Objects.requireNonNull(bb);
		synchronized (serverAddress) {
			uniqueContext.queueMessage(bb);
		}
	}
	
	/**
	 * Adds a private connection
	 * 
	 * @param otherClient the client with which a private connection must be created
	 * @param se the login client with the private context
	 */
	void addPrivateConnexion(String otherClient, SimpleEntry<PrivateContext, String> se ) {
		Objects.requireNonNull(otherClient);
		Objects.requireNonNull(se);
		privateConnexion.put(otherClient, se);
	}
	
	/**
	 * create the unique context, and connect it to the server
	 * main selector loop 
	 * @throws IOException
	 */
	public void launch() throws IOException {
		sc.configureBlocking(false);
		var key = sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueContext = new PublicContext(key, inputProcess, this);
		key.attach(uniqueContext);
		sc.connect(serverAddress);
		console.start();

		while (!Thread.interrupted()) {
			try {
				if (!sc.isOpen()) {
					selector.close();
					console.interrupt();
					return;
				}
				selector.select(this::treatKey);
				if (uniqueContext.isConnected() && uniqueContext.canSendCommand()) { // if the command can be processed by the client
					inputProcess.processUserInput();
				}
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}
	/**
	 * selector operations
	 * @param key the current key of the context
	 */
	private void treatKey(SelectionKey key) {
		var context = (Context) key.attachment();
		try {
			if (key.isValid() && key.isConnectable()) {
				context.doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				context.doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				context.doRead();
			}
		} catch (IOException ioe) {
			// lambda call in select requires to tunnel IOException
			logger.info("Closing sc");
			silentlyClose(key);
			throw new UncheckedIOException(ioe);
		}
	}


	/**
	 * Close the client connection
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

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 3) {
			usage();
			return;
		}
		new ClientOS(args[0], new InetSocketAddress(args[1], Integer.parseInt(args[2]))).launch();
	}

	private static void usage() {
		System.out.println("Usage : ClientOS login hostname port");
	}
}
