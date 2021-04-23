package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PrivateConnexionAccepted;


public class PrivateConnexionAcceptedReader extends AbstractCommonReader<PrivateConnexionAccepted> implements Reader<PrivateConnexionAccepted>{


	private Long id;
	private boolean readId = false;
	private final LongReader longReader = new LongReader();
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn || !readReceiver || !readId) {
        	
        	ProcessStatus srState;
        	if(!readLogIn || !readReceiver) {
        		 srState =  stringReader.process(bb);
        	}else {
        		 srState =  longReader.process(bb);
        	}

    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		login = stringReader.get();

        	} else if (!readReceiver) {
        		readReceiver = true;
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
        frame = new PrivateConnexionAccepted(login, receiver, id);
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	@Override
	public void reset() {
		super.reset();

		readId = false;
		longReader.reset();
	}

}
