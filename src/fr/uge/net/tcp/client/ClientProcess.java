package fr.uge.net.tcp.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.replies.Response.Codes;

class ClientProcess {
	static private int MAX_LOGIN_SIZE = 30;
	static private int MAX_MSG_SIZE = 1024;
	static private int MAX_BUFFER_SIZE = (Integer.BYTES*4)+((2*MAX_LOGIN_SIZE+MAX_MSG_SIZE)*Character.BYTES);
	static private Logger logger = Logger.getLogger(ClientProcess.class.getName());
	
	private final Charset UTF8 = Charset.forName("UTF8");
	private final String login;

	ClientProcess(String login){
		this.login = Objects.requireNonNull(login);
	}
	
	
	Optional<ByteBuffer> publicMessageBuff(String msg) {
		if(msg.length() >= MAX_MSG_SIZE) {
			System.out.println("user message is too long, ignored");
			return Optional.empty();
		}
		var bb = ByteBuffer.allocate((Integer.BYTES*3)+((MAX_LOGIN_SIZE+MAX_MSG_SIZE)*Character.BYTES));
		bb.putInt(Codes.PUBLIC_MESSAGE_SENT.getCode());
		bb.putInt(login.length()).put(UTF8.encode(login));
		bb.putInt(msg.length()).put(UTF8.encode(msg));
		return Optional.of(bb);
	}
	
	Optional<ByteBuffer> connectionBuffer(){
		var bb = ByteBuffer.allocate((2 * Integer.BYTES) + (MAX_LOGIN_SIZE * Character.BYTES));
		bb.putInt(Codes.REQUEST_CONNECTION.getCode());
		bb.putInt(login.length()).put(UTF8.encode(login));
		return Optional.of(bb);
	}
	
	




}
