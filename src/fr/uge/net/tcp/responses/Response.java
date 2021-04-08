package fr.uge.net.tcp.responses;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Response {
	final Charset UTF8 = Charset.forName("utf8");
	static enum Codes {		
		 REQUEST_SERVER_CONNECTION(0), 
		 PUBLIC_MESSAGE_SENT(1),
		 PRIVATE_MESSAGE_SENT(2),
		 PUBLIC_MESSAGE_RECEIVED(3),
		 REQUEST_PRIVATE_CONNEXION(4),
		 ID_PRIVATE(5),
		 ESTABLISHED(6),
		 PRIVATE_MESSAGE_RECEIVED(7),
		 LOGIN_PRIVATE(8),
		 DISCONNECT_PRIVATE(9),
		 LOGIN_ACCEPTED(10),
		 LOGIN_REFUSED(11),
		 ACCEPT_PRIVATE_CONNEXION(12),
		 REFUSE_PRIVATE_CONNEXION(13);
				
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
