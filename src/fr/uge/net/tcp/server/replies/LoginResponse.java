package fr.uge.net.tcp.server.replies;

import java.nio.ByteBuffer;

public class LoginResponse implements Response  {
	
	private final ResponseCodes responseCode;

	public LoginResponse(ResponseCodes responseCodes){
		if(responseCodes != ResponseCodes.LOGIN_ACCEPTED
			&& responseCodes != ResponseCodes.LOGIN_REFUSED) {
			throw new IllegalArgumentException("Code not valide");
		}
		responseCode = responseCodes;
	}

	@Override
	public ResponseCodes getResponseCode() {
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