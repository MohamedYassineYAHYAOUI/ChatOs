package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MessageResponse implements Response {
	public static class Builder{
		private String targetLogin;
		private String message;
		private String login;
		private Codes messageCode;
		private static int MAX_LOGIN_SIZE = 30;
		private static int MAX_MSG_SIZE = 1024;
	
		public Builder setTargetLogin(String targetLogin) {
			Objects.requireNonNull(targetLogin);
			if(targetLogin.isEmpty() || targetLogin.isBlank() ) {
				throw new IllegalArgumentException("Target login is invalid");
			}
			if(targetLogin.length() >  MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("Target login is too long");
			}
			this.targetLogin = targetLogin;
			return this;
		}
		
		public Builder setLogin(String login) {
			Objects.requireNonNull(login);
			if(login.isEmpty() || login.isBlank() ) {
				throw new IllegalArgumentException("login is invalid");
			}
			if(login.length() >  MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("login too long");
			}
			this.login = login;
			return this;
		}
		
		public Builder setMessage(String message) {
			Objects.requireNonNull(message);
			if(message.length() >  MAX_MSG_SIZE) {
				throw new IllegalArgumentException("message too long");
			}
			this.message = message;
			return this;
		}
	
		public Builder setPacketCode(Codes messageCode) {
			Objects.requireNonNull(messageCode);
			this.messageCode = messageCode;
			return this;
		}
	
		public MessageResponse build(){
			if(messageCode == null ) {
				throw new IllegalStateException("packet is missing code");
			}
			if(login == null) {
				throw new IllegalStateException("packet is missing login");
			}
			MessageResponse messageResponse = null;
			if(targetLogin != null && message != null) {
				messageResponse =  new MessageResponse(messageCode, login, targetLogin, message );	
			}else if( message != null) {
				messageResponse = new MessageResponse(messageCode, login, message);
			}else {
				messageResponse = new MessageResponse(messageCode, login);
			}
			resetBuilder();
			return messageResponse;
		}
	
		private void resetBuilder() {
			targetLogin = null;
			message = null;
			login = null;
			messageCode = null;
		}
		
	}
	private String targetLogin;
	private final String login;
	private String message;
	private final Codes messageCode;
		
	private MessageResponse(Codes code, String login) {
		this.login = Objects.requireNonNull(login);
		this.messageCode = code;
	}
	
	private MessageResponse(Codes code, String login, String message) {
		this(code, login);
		this.message = Objects.requireNonNull(message);
		
	}
	
	private MessageResponse(Codes code, String login, String targetLogin, String message) {
		this(code, login, message);
		this.targetLogin = Objects.requireNonNull(targetLogin);
	}
	

	
	@Override
	public Codes getResponseCode() {
		return messageCode;
	}

	@Override
	public int size() {
		var size = 2 * Integer.BYTES + ( login.length()* Character.BYTES);
		if(message != null) {
			size +=Integer.BYTES + (message.length()* Character.BYTES);
		}
		
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
		if(message != null) {
			internalBuffer.putInt(message.length()).put(UTF8.encode(message));
		}

		return internalBuffer;
	}
}
