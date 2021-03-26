package fr.uge.net.tcp.server;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import fr.uge.net.tcp.server.MessageReader.State;
import fr.uge.net.tcp.server.Reader.ProcessStatus;

class OperationManager implements Reader<Operation> {
	
	private enum State {
		DONE, WAITING, ERROR
	};
	
	static private Logger logger = Logger.getLogger(OperationManager.class.getName());
	private final IntReader intReader = new IntReader();
	private boolean readCode = false;
	private boolean readOperation = false;
	private State state = State.WAITING;
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readCode || !readOperation) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		builder.setLogin(stringReader.get());

        	}else {
        		readMsg = true;
        		builder.setMessage(stringReader.get());
        		stringReader.reset();
        		
        	}
    		stringReader.reset();
        	
        }

        message = builder.build();
        state=State.DONE;
        return ProcessStatus.DONE;
	}
	
	
	
}
