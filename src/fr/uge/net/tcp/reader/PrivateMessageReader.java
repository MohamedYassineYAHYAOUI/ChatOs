package fr.uge.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import fr.uge.net.tcp.reader.Reader.ProcessStatus;

public class PrivateMessageReader implements Reader<String> {
	private enum State {
		DONE, WAITING, ERROR
	};
	static private Logger logger = Logger.getLogger(MessageReader.class.getName());
	private State state = State.WAITING;
	private final StringReader stringReader = new StringReader();
	private String message = null;
	private String senderLogin = null;
	private String targetLogin = null;
	private boolean readSenderLogIn = false;
	private boolean readTargetLogIn = false;
	private boolean readMsg = false;
	
	

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readSenderLogIn || !readTargetLogIn || !readMsg) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readSenderLogIn) {
        		readSenderLogIn = true;
        		senderLogin = stringReader.get();

        	} else if (!readTargetLogIn) {
        		readTargetLogIn = true;
        		targetLogin = stringReader.get();        		
        	}
        	else {
        		readMsg = true;
        		message = stringReader.get();
        		stringReader.reset();	
        	}
    		stringReader.reset();
        	
        }
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public String get() {
		throw new IllegalStateException("use getMessage() or getLogin()");
	}
	
	public String getMessage() {
		if(state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return message;
	}
	
	public String getSenderLogin() {
		if(state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return senderLogin;
	}
	
	public String getTargetLogin() {
		if(state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return targetLogin;
	}
	
	@Override
	public void reset() {
		state = State.WAITING;
		stringReader.reset();
		message= null;
		readSenderLogIn = false;
		readTargetLogIn = false;
		readMsg = false;
	}
}
