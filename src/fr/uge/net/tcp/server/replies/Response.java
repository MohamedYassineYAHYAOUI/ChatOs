package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;

public interface Response {

	static enum ResponseCodes {
		LOGIN_ACCEPTED(10), LOGIN_REFUSED(11);

		private int code;

		ResponseCodes(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	ResponseCodes getResponseCode();

	int size();

	ByteBuffer getResponseBuffer();

}
