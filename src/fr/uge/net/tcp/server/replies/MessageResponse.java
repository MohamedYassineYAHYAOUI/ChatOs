package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MessageResponse implements Response {
	static class Builder{
		private String targetLogin;
		private String message;
		private String login;
		private Codes messageCode;
		private static int MAX_LOGIN_SIZE = 30;
		private static int MAX_MSG_SIZE = 1024;
	
		Builder setTargetLogin(String targetLogin) {
			Objects.requireNonNull(targetLogin);
			if(targetLogin.length() >  MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("senderLogin too long");
			}
			this.targetLogin = targetLogin;
			return this;
		}
		
		Builder setLogin(String login) {
			Objects.requireNonNull(login);
			if(login.length() >  MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("login too long");
			}
			this.login = login;
			return this;
		}
		
		Builder setMessage(String message) {
			Objects.requireNonNull(message);
			if(message.length() >  MAX_MSG_SIZE) {
				throw new IllegalArgumentException("message too long");
			}
			this.message = message;
			return this;
		}
	
		Builder setPacketCode(Codes messageCode) {
			Objects.requireNonNull(messageCode);
			this.messageCode = messageCode;
			return this;
		}
	
		MessageResponse build(){
			if(messageCode == null ) {
				throw new IllegalStateException("packet is missing code");
			}
			if(message == null) {
				throw new IllegalStateException("packet is missing message");
			}
			if(login == null) {
				throw new IllegalStateException("packet is missing login");
			}
			if(targetLogin != null) {
				return new MessageResponse(messageCode, login, targetLogin, message );	
			}else {
				return new MessageResponse(messageCode, login, message);
			}
		}
	
		void resetBuilder() {
			targetLogin = null;
			message = null;
			login = null;
			messageCode = null;
		}
		
	}
	private String targetLogin;
	private final String login;
	private final String message;
	private final Codes messageCode;
		
	
	private MessageResponse(Codes code, String login, String message) {
		this.login = Objects.requireNonNull(login);
		this.message = Objects.requireNonNull(message);
		this.messageCode = code;
	}
	
	private MessageResponse(Codes code, String login, String targetLogin, String message) {
		this(code, login, message);
		this.targetLogin = Objects.requireNonNull(message);
	}
	
	
	@Override
	public Codes getResponseCode() {
		return messageCode;
	}

	@Override
	public int size() {
		var size = 3 * Integer.BYTES + ((message.length() + login.length())* Character.BYTES);
		if(targetLogin != null) {
			size+= Integer.BYTES + (targetLogin.length() * Character.BYTES);
		}
		return size;
		
	}

	@Override
	public ByteBuffer getResponseBuffer() {
		var internalBuffer = ByteBuffer.allocate(size());
		internalBuffer.putInt(messageCode.getCode());
		internalBuffer.putInt(login.length()).put(UTF8.encode(login));
		if(targetLogin != null) {
			internalBuffer.putInt(targetLogin.length()).put(UTF8.encode(targetLogin));
		}
		internalBuffer.putInt(message.length()).put(UTF8.encode(message));

		return internalBuffer;
	}
}
