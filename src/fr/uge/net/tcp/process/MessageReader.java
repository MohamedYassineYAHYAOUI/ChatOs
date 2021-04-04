package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.logging.Logger;


class MessageReader extends CommonMessageReader<String> implements Reader<String> {
	//string - stirng

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
	
	String getMessage() {
		return super.getMessage();
	}
	

	String getLogin() {
		return super.getLogin();
	}

	
	@Override
	public void reset() {
		packetReset();
	}

}
