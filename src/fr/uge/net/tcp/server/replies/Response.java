package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Response {
	final Charset UTF8 = Charset.forName("utf8");
	static enum Codes {
		LOGIN_ACCEPTED(10), LOGIN_REFUSED(11), PUBLIC_MESSAGE_SENT(1),
		PUBLIC_MESSAGE_RECEIVED(3),REQUEST_SERVER_CONNECTION(0), PRIVATE_MESSAGE_SENT(2),
		PRIVATE_MESSAGE_RECEIVED(7), REQUEST_PRIVATE_CONNEXION(4), ACCEPT_PRIVATE_CONNEXION(12),
		REFUSE_PRIVATE_CONNEXION(13), ID_PRIVATE(5), ESTABLISHED(6), LOGIN_PRIVATE(8);
		
		
		private int code;

		private Codes(int code) {
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
