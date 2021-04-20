package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;


import fr.uge.net.tcp.visitor.PrivateConnexionRequest;


public class PrivatConnexionRequestReader implements Reader<PrivateConnexionRequest>{

	
	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private PrivateConnexionRequest privateConnexionRequest; 
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
        privateConnexionRequest = new PrivateConnexionRequest(requester, target);
        return ProcessStatus.DONE;
	}

	@Override
	public PrivateConnexionRequest get() {
		return privateConnexionRequest;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		privateConnexionRequest = null; 
		requester = null;
		target = null;
		readRequester = false;
		readTarget = false;
		stringReader.reset();
	}

	
	
}
