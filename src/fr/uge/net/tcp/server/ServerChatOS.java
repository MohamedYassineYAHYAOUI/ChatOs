package fr.uge.net.tcp.server;

import java.io.IOException;


public class ServerChatOS {
	/**
	 * Main method to launch the server
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 1) {
			usage();
			return;
		}
		new Server(Integer.parseInt(args[0])).launch();
	}

	/**
	 * Displays the port ServerChatOS used
	 */
	private static void usage() {
		System.out.println("Usage : ServerChatOS port");
	}
}
