package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Response {
	final Charset UTF8 = Charset.forName("utf8");
	static enum Codes {
		LOGIN_ACCEPTED(10), LOGIN_REFUSED(11), PUBLIC_MESSAGE(1),
		REQUEST_CONNECTION(0);
		
		
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
