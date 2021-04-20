package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PublicMessage;

public class PublicMessageReader implements Reader<PublicMessage>{
	
	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private PublicMessage publicMessage; 
	private String login;
	private String message;
	private boolean readLogIn = false;
	private boolean readMsg = false;
	private final StringReader stringReader = new StringReader(); 
	
	
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
        publicMessage = new PublicMessage(login, message);
        return ProcessStatus.DONE;
	}

	@Override
	public PublicMessage get() {
		return publicMessage;
	}

	@Override
	public void reset() {
		publicMessage = null;
		state = State.WAITING;
		login = null;
		message = null;
		readLogIn = false;
		readMsg = false;
		stringReader.reset();
	} 

	
	
	
	
}
