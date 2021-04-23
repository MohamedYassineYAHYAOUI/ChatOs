package fr.uge.net.tcp.responses;

import java.nio.ByteBuffer;
import java.util.Objects;

public class MessageResponse implements Response {
	public static class Builder {
		private String targetLogin;
		private String message;
		private String login;
		private Codes messageCode;
		private boolean setId = false;
		private long connexionId;
		private static int MAX_LOGIN_SIZE = 30;
		private static int MAX_MSG_SIZE = 1024;

		/**
		 * Sets the target login for the builder of a message response packet 
		 * @param targetLogin
		 * @return the builder
		 */
		public Builder setTargetLogin(String targetLogin) {
			Objects.requireNonNull(targetLogin);
			if (targetLogin.isEmpty() || targetLogin.isBlank()) {
				throw new IllegalArgumentException("Target login is invalid");
			}
			if (targetLogin.length() > MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("Target login is too long");
			}
			this.targetLogin = targetLogin;
			return this;
		}

		/**
		 * Set the connect id for the builder of the message response packet
		 * 
		 * @param connexionId
		 * @return the builder
		 */
		public Builder setId(Long connexionId) {
			this.connexionId = connexionId;
			this.setId = true;
			return this;
		}

		/**
		 * Sets the login for the builder of the message response packet
		 * 
		 * @param login
		 * @return the builder
		 */
		public Builder setLogin(String login) {
			Objects.requireNonNull(login);
			if (login.isEmpty() || login.isBlank()) {
				throw new IllegalArgumentException("login is invalid");
			}
			if (login.length() > MAX_LOGIN_SIZE) {
				throw new IllegalArgumentException("login too long");
			}
			this.login = login;
			return this;
		}

		/**
		 * Sets the message for the builder of the message response
		 * 
		 * @param message
		 * @return 
		 */
		public Builder setMessage(String message) {
			Objects.requireNonNull(message);
			if (message.length() > MAX_MSG_SIZE) {
				throw new IllegalArgumentException("message too long");
			}
			this.message = message;
			return this;
		}

		/**
		 * Sets the code of the packet for the builder of the message response
		 * 
		 * @param messageCode
		 * @return the builder
		 */
		public Builder setPacketCode(Codes messageCode) {
			Objects.requireNonNull(messageCode);
			this.messageCode = messageCode;
			return this;
		}

		/**
		 * Build the message response packet 
		 * 
		 * @return the message response packet
		 */
		public MessageResponse build() {

			MessageResponse messageResponse = new MessageResponse(messageCode, login, targetLogin, message,
					connexionId, setId);
			resetBuilder();
			return messageResponse;
		}

		/**
		 * Resets all fields of the builder
		 */
		public void resetBuilder() {
			targetLogin = null;
			message = null;
			login = null;
			messageCode = null;
			setId = false;
			connexionId = 0;
		}

	}

	private String targetLogin;
	private final String login;
	private String message;
	private final Codes messageCode;
	private final Long connexionId;
	private final boolean setId;

	private MessageResponse(Codes code, String login, String targetLogin, String message, long connexionId, boolean setId) {
		this.messageCode = code;
		this.login = login;
		this.targetLogin = targetLogin;
		this.message = message;
		this.connexionId = connexionId;
		this.setId = setId;
	}



	@Override
	public Codes getResponseCode() {
		return messageCode;
	}

	@Override
	public int size() {
		int size =0;
		if(messageCode != null) {
			size += Integer.BYTES;
		}
		if (login != null) {
			size += Integer.BYTES + login.length() * Character.BYTES;
		}
		if (message != null) {
			size += Integer.BYTES + (message.length() * Character.BYTES);
		}

		if (targetLogin != null) {
			size += Integer.BYTES + (targetLogin.length() * Character.BYTES);
		}
		if (setId) {
			size += Long.BYTES;
		}
		return size;

	}

	@Override
	public ByteBuffer getResponseBuffer() {
		var internalBuffer = ByteBuffer.allocate(size());
		if(messageCode != null) {
			internalBuffer.putInt(messageCode.getCode());
		}
		if (login != null) {
			internalBuffer.putInt(login.length()).put(UTF8.encode(login));
		}
		if (targetLogin != null) {
			internalBuffer.putInt(targetLogin.length()).put(UTF8.encode(targetLogin));
		}
		if (message != null) {
			internalBuffer.putInt(message.length()).put(UTF8.encode(message));
		}
		if (setId) {
			internalBuffer.putLong(connexionId);
		}

		return internalBuffer;
	}
}
