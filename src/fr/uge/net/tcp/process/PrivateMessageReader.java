package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;

/***
 * Object that reads a buffer with the format :
 * String - String - T
 * @param <T> type of the value (or the message) sent in this buffer
 */
class PrivateMessageReader<T> extends CommonMessageReader<T> implements Reader<String> {

	private String targetLogin = null;

	private boolean readTargetLogIn = false;
	private final Reader<T> elemReader; 
	PrivateMessageReader(Reader<T> elemReader){
		this.elemReader = Objects.requireNonNull(elemReader);
	}
	

	@Override
	 public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn || !readTargetLogIn || !readMsg) {
        	
        	ProcessStatus srState ;
        	if(!readLogIn || !readTargetLogIn  ) {
        		srState= stringReader.process(bb);
        	}else {
        		srState= elemReader.process(bb);
        	}
        	
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		login = stringReader.get();

        	} else if (!readTargetLogIn) {
        		readTargetLogIn = true;
        		targetLogin = stringReader.get();  
        		stringReader.reset();	
        	}
        	else {
        		readMsg = true;
        		message = elemReader.get();
        	}
    		stringReader.reset();
    		elemReader.reset();
        }
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public String get() {
		throw new IllegalStateException("use getMessage() or getLogin() or getTargetLogin()");
	}

	 T getMessage() {
		return super.getMessage();
	}
	
	
	 String getSenderLogin() {
		return getLogin();
	}
	
	 String getTargetLogin() {
		if(state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return targetLogin;
	}
	
	@Override
	public void reset() {
		packetReset();
		elemReader.reset();
		readTargetLogIn = false;
	}
}
