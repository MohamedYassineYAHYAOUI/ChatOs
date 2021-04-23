package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PrivateConnexionAccepted;


public class PrivateConnexionAcceptedReader  implements Reader<PrivateConnexionAccepted>{

	
	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private PrivateConnexionAccepted privateConnexionAccepted; 
	private String sender;
	private String  receiver;
	private Long id;
	private boolean readLoginSender = false;
	private boolean readLoginreciever = false;
	private boolean readId = false;
	private final StringReader stringReader = new StringReader();
	private final LongReader longReader = new LongReader();
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLoginSender || !readLoginreciever || !readId) {
        	
        	ProcessStatus srState;
        	if(!readLoginSender || !readLoginreciever) {
        		 srState =  stringReader.process(bb);
        	}else {
        		 srState =  longReader.process(bb);
        	}

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
        		readId = true;
        		id = longReader.get();
        	}
    		stringReader.reset();
    		longReader.reset();
        }
        privateConnexionAccepted = new PrivateConnexionAccepted(sender, receiver, id);
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public PrivateConnexionAccepted get() {
		if(state != State.DONE) {
			throw new IllegalStateException("State is not Done");
		}
		return privateConnexionAccepted;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		privateConnexionAccepted = null; 
		sender = null;
		receiver = null;
		readLoginSender = false;
		readLoginreciever = false;
		readId = false;
		stringReader.reset();
		longReader.reset();
	}

}
