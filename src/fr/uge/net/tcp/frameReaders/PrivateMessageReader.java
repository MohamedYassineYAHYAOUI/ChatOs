package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PrivateMessage;

public class PrivateMessageReader implements Reader<PrivateMessage>{

	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private PrivateMessage privateMessage; 
	private String sender;
	private String  receiver;
	private String message;
	private boolean readLoginSender = false;
	private boolean readLoginreciever = false;
	private boolean readMsg = false;
	private final StringReader stringReader = new StringReader();
	

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLoginSender || !readLoginreciever || !readMsg) {
        	
        	ProcessStatus srState =  stringReader.process(bb);

    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLoginSender) {
        		readLoginSender = true;
        		sender = stringReader.get();

        	} else if (!readLoginreciever) {
        		readLoginreciever = true;
        		receiver = stringReader.get();  
        		stringReader.reset();	
        	}
        	else {
        		readMsg = true;
        		message = stringReader.get();
        	}
    		stringReader.reset();
        }
        privateMessage = new PrivateMessage(sender, receiver, message);
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public PrivateMessage get() {
		return privateMessage;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		privateMessage = null; 
		sender = null;
		receiver = null;
		message = null;
		readLoginSender = false;
		readLoginreciever = false;
		readMsg = false;
		stringReader.reset();
	}
	

	
}
