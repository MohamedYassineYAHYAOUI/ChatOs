package fr.uge.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.logging.Logger;


public class MessageReader extends PacketRreader implements Reader<String> {


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
		return super.getMessage();
	}
	

	public String getLogin() {
		return super.getLogin();
	}

	
	@Override
	public void reset() {
		packetReset();
	}

}
