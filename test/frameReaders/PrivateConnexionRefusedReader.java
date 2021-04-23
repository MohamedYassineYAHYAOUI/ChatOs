package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PrivateConnexionRefused;


public class PrivateConnexionRefusedReader{
/*
	
	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private PrivateConnexionRefused privateConnexionRefused; 
	private String requester;
	private String target;
	private boolean readRequester = false;
	private boolean readTarget = false;
	private final StringReader stringReader = new StringReader(); 
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readRequester || !readTarget) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readRequester) {
        		readRequester = true;
        		requester = stringReader.get();

        	}else {
        		readTarget = true;
        		target = stringReader.get();
        		stringReader.reset();	
        	}
    		stringReader.reset();
        	
        }
        state=State.DONE;
        privateConnexionRefused = new PrivateConnexionRefused(requester, target);
        return ProcessStatus.DONE;
	}

	@Override
	public PrivateConnexionRefused get() {
		return privateConnexionRefused;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		privateConnexionRefused = null; 
		requester = null;
		target = null;
		readRequester = false;
		readTarget = false;
		stringReader.reset();
	}
*/
}
