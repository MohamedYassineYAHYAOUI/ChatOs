package fr.uge.net.tcp.server;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.logging.Logger;
import fr.uge.net.tcp.reader.*;

class IdentificationProcess implements Reader<String>{
	private enum State {
		DONE, WAITING, ERROR
	};
	static private Logger logger = Logger.getLogger(MessageReader.class.getName());
	private State state = State.WAITING;
	private final StringReader stringReader = new StringReader();
	//private final Message.Builder builder = new Message.Builder();
	private String  login = null;
	private boolean readLogIn = false;
	private final Server server;
	
	IdentificationProcess(Server server){
		this.server = Objects.requireNonNull(server);
	}
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		login = stringReader.get();
        		//builder.setLogin(stringReader.get());
        	}
    		stringReader.reset();
        }
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public String get() {
		if(state != State.DONE ) {
			throw new IllegalStateException();
		}
		return login;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		stringReader.reset();
		login= null;
		readLogIn = false;
	}

	
	
}
