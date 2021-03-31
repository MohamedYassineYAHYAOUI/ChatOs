package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;

public class LoginResponse implements Response  {
	
	private final Codes responseCode;

	public LoginResponse(Codes responseCodes){
		if(responseCodes != Codes.LOGIN_ACCEPTED
			&& responseCodes != Codes.LOGIN_REFUSED) {
			throw new IllegalArgumentException("Code not valide");
		}
		responseCode = responseCodes;
	}

	@Override
	public Codes getResponseCode() {
		return responseCode;
	}

	@Override
	public int size() {
		return Integer.BYTES;
	}

	@Override
	public ByteBuffer getResponseBuffer() {
		var internalBuff = ByteBuffer.allocate(Integer.BYTES);
		internalBuff.putInt(responseCode.getCode());
		return internalBuff;
	}
}