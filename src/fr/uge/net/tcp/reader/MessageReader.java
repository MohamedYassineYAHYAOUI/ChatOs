package fr.uge.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.logging.Logger;


public class MessageReader implements Reader<String> {

	private enum State {
		DONE, WAITING, ERROR
	};
	static private Logger logger = Logger.getLogger(MessageReader.class.getName());
	private State state = State.WAITING;
	private final StringReader stringReader = new StringReader();
	private String message = null;
	private String login = null;
	private boolean readLogIn = false;
	private boolean readMsg = false;
	
	

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn || !readMsg) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		login = stringReader.get();

        	}else {
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
		return message;
	}
	
	public String getLogin() {
		return login;
	}
	
	@Override
	public void reset() {
		state = State.WAITING;
		stringReader.reset();
		message= null;
		readLogIn = false;
		readMsg = false;
	}

}
