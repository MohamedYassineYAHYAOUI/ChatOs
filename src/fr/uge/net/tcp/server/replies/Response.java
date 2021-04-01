package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Response {
	final Charset UTF8 = Charset.forName("utf8");
	static enum Codes {
		LOGIN_ACCEPTED(10), LOGIN_REFUSED(11), PUBLIC_MESSAGE_SENT(1),
		PUBLIC_MESSAGE_RECEIVED(3),REQUEST_CONNECTION(0), PRIVATE_MESSAGE_SENT(2),
		PRIVATE_MESSAGE_RECEIVED(7);
		
		
		private int code;

		Codes(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	Codes getResponseCode();

	int size();

	ByteBuffer getResponseBuffer();

}
