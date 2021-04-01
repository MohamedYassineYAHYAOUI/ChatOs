package fr.uge.net.tcp.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.replies.Response.Codes;

public class ClientOS {
	// static private int MAX_LOGIN_SIZE = 30;
	static private final Charset UTF8 = Charset.forName("utf8");
	static private Logger logger = Logger.getLogger(ClientOS.class.getName());

	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private final String login;
	private final Thread console;
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	private final Path folderPath;
	private Context uniqueContext;
	private final ClientProcess clientProcess;

	public ClientOS(String login, Path folderPath, InetSocketAddress serverAddress) throws IOException {
		this.folderPath = Objects.requireNonNull(folderPath);
		this.serverAddress = Objects.requireNonNull(serverAddress);
		this.login = Objects.requireNonNull(login);
		this.clientProcess = new ClientProcess(this.login);
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
				commandQueue.add(msg);
			}
			selector.wakeup();
		} catch (IllegalStateException e) {
			logger.warning("queue is full, msg ignored");
		}
	}

	private void processConnection() {
		synchronized (serverAddress) {
			var Optionalbb = clientProcess.connectionBuffer();
			if (Optionalbb.isEmpty()) {
				return;
			}
			uniqueContext.queueMessage(Optionalbb.get());
		}
		selector.wakeup();
	}

	/**
	 * Processes the command from commandQueue
	 */

	private void processCommands() {
		// intrestOPS
		synchronized (serverAddress) {

			if (uniqueContext.isConnected()) {
				while (!commandQueue.isEmpty()) {
					Optional<ByteBuffer> pmOptional = Optional.empty();
					var msg = commandQueue.poll();
					if (msg.startsWith("/")) {
						// connexion privée
					} else if (msg.startsWith("@")) {
						// msg privée
					    var elem = msg.split(" ")[0];
					   var targetLogin = elem.substring(1);
					   pmOptional = clientProcess.privateMessageBuff(msg, targetLogin);
					   if(pmOptional.isEmpty()) {
						   continue;
					   }
					   
					} else {
						pmOptional = clientProcess.publicMessageBuff(msg);
						if (pmOptional.isEmpty()) {
							continue;
						}
					}
					uniqueContext.queueMessage(pmOptional.get());

					// bb.putInt(login.length()).put(Context.UTF8.encode(login));
					// bb.putInt(msg.length()).put(Context.UTF8.encode(msg));
					// uniqueContext.queueMessage(bb);
				}
			}

		}
	}

	public void launch() throws IOException {
		sc.configureBlocking(false);
		var key = sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueContext = new Context(key);
		key.attach(uniqueContext);
		sc.connect(serverAddress);

		console.start();

		while (!Thread.interrupted()) {
			try {
				printKeys(); // for debug
				System.out.println("Starting select");

				if (!sc.isOpen()) {
					selector.close();
					console.interrupt();
					return;
				}

				selector.select(this::treatKey);

				if (uniqueContext.isConnected()) {

					processCommands();
				}

				System.out.println("Select finished");
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	private void treatKey(SelectionKey key) {
		printSelectedKey(key);
		try {
			if (key.isValid() && key.isConnectable()) {
				uniqueContext.doConnect();
			}
			if (key.isValid() && key.isWritable()) {

				uniqueContext.doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				uniqueContext.doRead();
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
		if (args.length != 4) {
			usage();
			return;
		}
		if (args[1].length() > 30) {
			System.out.println("login is too long ");
			return;
		}
		new ClientOS(args[1], Path.of(args[0]), new InetSocketAddress(args[2], Integer.parseInt(args[3]))).launch();
	}

	private static void usage() {
		System.out.println("Usage : ClientOS folder login hostname port");
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

	private String remoteAddressToString(SocketChannel sc) {
		try {
			return sc.getRemoteAddress().toString();
		} catch (IOException e) {
			return "???";
		}
	}

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
