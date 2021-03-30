package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Response {
	final Charset UTF8 = Charset.forName("utf8");
	enum ResponseCodes {
		LOGIN_ACCEPTED(10), LOGIN_REFUSED(11), PUBLIC_MESSAGE(1);

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
