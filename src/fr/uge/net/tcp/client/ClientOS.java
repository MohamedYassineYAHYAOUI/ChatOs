package fr.uge.net.tcp.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import fr.uge.net.tcp.responses.MessageResponse;
import fr.uge.net.tcp.responses.Response.Codes;

public class ClientOS {
	static private Logger logger = Logger.getLogger(ClientOS.class.getName());

	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private final String login;
	private final Thread console;
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	private final HashMap<String, SimpleEntry<PrivateContext, String>> privateConnexion = new HashMap<>();

	private Context uniqueContext;
	private final MessageResponse.Builder packetBuilder;

	public ClientOS(String login, InetSocketAddress serverAddress) throws IOException {
		this.serverAddress = Objects.requireNonNull(serverAddress);
		this.login = Objects.requireNonNull(login);
		this.packetBuilder = new MessageResponse.Builder();
		this.sc = SocketChannel.open();
		this.selector = Selector.open();
		this.console = new Thread(this::consoleRun);
	}

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
	 *
	 * @param msg
	 * @throws InterruptedException
	 */

	private void sendCommand(String msg) throws InterruptedException {
		Objects.requireNonNull(msg);
		try {
			synchronized (serverAddress) {
				if (!msg.isEmpty() && !msg.isBlank()) {
					commandQueue.add(msg);
				}
			}
			selector.wakeup();
		} catch (IllegalStateException e) {
			logger.warning("queue is full, msg ignored");
		}
	}

	private void processConnection() {
		synchronized (serverAddress) {
			packetBuilder.setPacketCode(Codes.REQUEST_SERVER_CONNECTION).setLogin(login);
			uniqueContext.queueMessage(packetBuilder.build().getResponseBuffer());
		}
		selector.wakeup();
	}

	void removePrivateConnection(String targetLogin) {
		privateConnexion.remove(targetLogin);
	}

	void createPrivateConnection(String targetLogin, long id) {
		SocketChannel sc;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			var key = sc.register(selector, SelectionKey.OP_CONNECT);
			String msg = null;
			var pc = privateConnexion.get(targetLogin);
			if (pc != null) {
				msg = pc.getValue();
			}
			synchronized (serverAddress) {
				sc.connect(serverAddress);
				var pccontext = PrivateContext.CreateContext(key, targetLogin, id, msg);
				key.attach(pccontext);
				privateConnexion.put(targetLogin, new SimpleEntry<PrivateContext, String>(pccontext, null));

			}


		} catch (IOException e) {
			logger.warning("error while creating private connection " + e.getMessage());
		}
	}

	void requestPrivateConnection(String login_requester, String login_target, int threadOrder) {
		if (!login_target.equals(login)) {
			return;
		}
		System.out.println("private connexion request form " + login_requester + ": Accept(Y) or refuse(N)");
		new Thread(() -> {
			try {
				uniqueContext.setCanSendCommand(false);
				while (!Thread.interrupted()) {
					if (!commandQueue.isEmpty() && uniqueContext.currentThreadOrder() == threadOrder) {
						var response = commandQueue.poll();
						if (response.toUpperCase().equals("Y")) {
							System.out.println("ACCEPTED");
							packetBuilder.setPacketCode(Codes.ACCEPT_PRIVATE_CONNEXION).setLogin(login_requester)
									.setTargetLogin(login_target);
						} else if (response.toUpperCase().equals("N")) {
							System.out.println("REFUSED");
							packetBuilder.setPacketCode(Codes.REFUSE_PRIVATE_CONNEXION).setLogin(login_requester)
									.setTargetLogin(login_target);
						} else {

							System.out.println("invalid choice, private connexion request form " + login_requester
									+ ": Accept(Y) or refuse(N)");
							continue;
						}
						synchronized (commandQueue) {
							uniqueContext.queueMessage(packetBuilder.build().getResponseBuffer());
							uniqueContext.doWrite();
							uniqueContext.setCanSendCommand(true);
						}
						return;
					}
				}
			} catch (Exception e) {
				uniqueContext.setCanSendCommand(true);
				logger.warning("Thread private connexion inturrepted " + e);
				return;
			}
		}).start();

	}

	/**
	 * Processes the command from commandQueue
	 */

	private void processCommands() {
		// intrestOPS

		while (!commandQueue.isEmpty()) {

			try {

				// ByteBuffer message = null ;
				var msg = commandQueue.poll();
				String targetLogin = null;

				if (msg.startsWith("/")) {
					var tmp = msg.split(" ");
					targetLogin = tmp[0].substring(1); // target Login
					if (targetLogin.equals(login)) {
						continue;
					}
					var pc = privateConnexion.get(targetLogin);
					if (tmp.length == 1 && pc != null) {
						if (pc.getKey() != null) {
							uniqueContext.queueMessage(packetBuilder.setPacketCode(Codes.DISCONNECT_PRIVATE)
									.setId(pc.getKey().getId()).build().getResponseBuffer());
						}
						removePrivateConnection(targetLogin);
						continue;
					}
					String message = msg.substring(targetLogin.length() + 2);

					if (pc == null) { // no request for the target
						packetBuilder.setPacketCode(Codes.REQUEST_PRIVATE_CONNEXION).setLogin(login)
								.setTargetLogin(targetLogin); // buffer builder
						privateConnexion.put(targetLogin, new SimpleEntry<PrivateContext, String>(null, message));
					} else {
						if (pc.getKey() != null) { // connection established
							pc.getKey().queueMessage(packetBuilder.setMessage(message).build().getResponseBuffer());
						}
						continue;
					}

				} else if (msg.startsWith("@")) {// message privée
					targetLogin = msg.split(" ")[0].substring(1);
					if (targetLogin.equals(login)) {
						continue;
					}
					packetBuilder.setPacketCode(Codes.PRIVATE_MESSAGE_SENT).setLogin(login).setTargetLogin(targetLogin)
							.setMessage(msg.substring(targetLogin.length() + 2)); // buffer builder
				} else { // message public
					packetBuilder.setPacketCode(Codes.PUBLIC_MESSAGE_SENT).setLogin(login).setMessage(msg);
				}
				if (targetLogin != null && targetLogin.equals(login)) {
					continue;
				}

				uniqueContext.queueMessage(packetBuilder.build().getResponseBuffer());

			} catch (StringIndexOutOfBoundsException e) {
				logger.info("invalide request, ignored");
			} catch (IllegalArgumentException e) {
				logger.info(e.getMessage());
			}
		}
	}

	public void launch() throws IOException {
		sc.configureBlocking(false);
		var key = sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueContext = new Context(key, login, this);
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
				if (uniqueContext.isConnected() && uniqueContext.canSendCommand()) {
					processCommands();
				}
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	private void treatKey(SelectionKey key) {
		var context = (GeneralContext) key.attachment();
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
