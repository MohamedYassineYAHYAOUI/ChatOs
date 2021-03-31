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
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.replies.Response.Codes;

public class ClientOS {
	static private int BUFFER_SIZE = 10_000;
	static private int MAX_LOGIN_SIZE = 30;
	static private final Charset UTF8 = Charset.forName("utf8");
	static private Logger logger = Logger.getLogger(ClientOS.class.getName());

	private final SocketChannel sc;
	private final Selector selector;
	private final InetSocketAddress serverAddress;
	private final String login;
	private String msg;
	private final Thread console;
	private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
	private final Path folderPath;
	private Context uniqueContext;

	public ClientOS(String login, Path folderPath, InetSocketAddress serverAddress) throws IOException {
		Objects.requireNonNull(login);
		if (login.length() > MAX_LOGIN_SIZE) {
			throw new IllegalArgumentException("login must be less then " + MAX_LOGIN_SIZE + " characters");
		}
		this.folderPath = Objects.requireNonNull(folderPath);
		this.serverAddress = Objects.requireNonNull(serverAddress);
		this.login = login;
		this.sc = SocketChannel.open();
		this.selector = Selector.open();
		this.console = new Thread(this::consoleRun);

	}

	private void consoleRun() {
		try {
			var scan = new Scanner(System.in);
			while (scan.hasNextLine()) {
				var msg = scan.nextLine();
				sendCommand(msg);
			}
		} catch (InterruptedException e) {
			logger.info("Console thread has been interrupted");
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
			// selector.wakeup();
		} catch (IllegalStateException e) {
			logger.warning("queue is full, msg ignored");
		}
	}

	/**
	 * Processes the command from commandQueue
	 */

	private void processCommands() {
		// intrestOPS
		synchronized (serverAddress) {
			if (uniqueContext.isConnected()) {
				while (!commandQueue.isEmpty()) {
					var msg = commandQueue.poll();
					// ->analyse de message

					// var bb = ByteBuffer.allocate(BUFFER_SIZE);
					// bb.putInt(login.length()).put(Context.UTF8.encode(login));
					// bb.putInt(msg.length()).put(Context.UTF8.encode(msg));
					// uniqueContext.queueMessage(bb);
				}
			}

		}
	}

	private void processConnection() {
		var bb = ByteBuffer.allocate(2 * Integer.BYTES + MAX_LOGIN_SIZE * Character.BYTES);
		bb.putInt(Codes.REQUEST_CONNECTION.getCode());
		bb.putInt(login.length()).put(UTF8.encode(login));
		System.out.println("queue message");
		uniqueContext.queueMessage(bb);
		// selector.wakeup();

	}

	public void launch() throws IOException {
		sc.configureBlocking(false);
		var key = sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueContext = new Context(key);
		key.attach(uniqueContext);
		sc.connect(serverAddress);

		// processConnection();

		console.start(); // aprés validation de connection

		while (!Thread.interrupted()) {
			try {
				printKeys(); // for debug
				System.out.println("Starting select");

				selector.select(this::treatKey);
				if (!uniqueContext.isConnected()) {
					processConnection();
				} else {
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
