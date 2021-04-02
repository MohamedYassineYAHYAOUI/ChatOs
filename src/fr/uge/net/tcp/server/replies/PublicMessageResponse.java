package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.util.Objects;

public class PublicMessageResponse implements Response {
/*
	private final String senderLogin;
	private final String message;

//	public PublicMessageResponse(String login, String message) {
//		this.senderLogin = Objects.requireNonNull(login);
//		this.message = Objects.requireNonNull(message);
//	}

	@Override
	public Codes getResponseCode() {
		return Codes.PUBLIC_MESSAGE_RECEIVED;
	}

	@Override
	public int size() {
		return 3 * Integer.BYTES + (senderLogin.length() + message.length()) * Character.BYTES;
	}

	@Override
	public ByteBuffer getResponseBuffer() {
		var internalBuffer = ByteBuffer.allocate(size());
		internalBuffer.putInt(Codes.PUBLIC_MESSAGE_RECEIVED.getCode());
		internalBuffer.putInt(senderLogin.length()).put(UTF8.encode(senderLogin));
		internalBuffer.putInt(message.length()).put(UTF8.encode(message));

		return internalBuffer;
	}
*/
}
