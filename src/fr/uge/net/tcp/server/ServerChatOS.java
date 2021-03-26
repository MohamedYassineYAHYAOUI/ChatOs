package fr.uge.net.tcp.server;

import java.io.IOException;

public class ServerChatOS {
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 1) {
			usage();
			return;
		}
		new Server(Integer.parseInt(args[0])).launch();
	}

	private static void usage() {
		System.out.println("Usage : ServerChaton port");
	}

}
