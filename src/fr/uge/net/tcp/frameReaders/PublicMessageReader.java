package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PublicMessage;

/**
 * 
 * receiver = message
 * readReceiver = redMsg
 *
 */
public class PublicMessageReader extends AbstractCommonReader<PublicMessage> implements Reader<PublicMessage>{

	
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn || !readReceiver) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readLogIn) {
        		readLogIn = true;
        		login = stringReader.get();

        	}else {
        		readReceiver = true;
        		receiver = stringReader.get();
        		stringReader.reset();	
        	}
    		stringReader.reset();
        	
        }
        state=State.DONE;
        frame = new PublicMessage(login, receiver);
        return ProcessStatus.DONE;
	}



	@Override
	public void reset() {
		super.reset();
	} 

	
	
	
	
}
