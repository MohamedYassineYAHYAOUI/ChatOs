package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.ServerConnection;

public class ServerConnectionReader {
/*
	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private ServerConnection serverConnection; 
	private String login;
	private boolean readLogIn = false;
	private final StringReader stringReader = new StringReader();
	
	
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
        	}
    		stringReader.reset();
        }
        state=State.DONE;
        serverConnection = new ServerConnection(login);
        return ProcessStatus.DONE;
	}

	@Override
	public ServerConnection get() {
		if(state != State.DONE) {
			throw new IllegalStateException("Server Connection Reader");
		}
		return serverConnection;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		serverConnection = null; 
		login = null;
		readLogIn = false;
		stringReader.reset();
	}
	*/
}
