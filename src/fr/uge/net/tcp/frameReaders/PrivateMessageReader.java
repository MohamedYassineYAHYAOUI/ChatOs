package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.visitor.PrivateMessage;


/**
 * Reader for the packet login (String) receiver (String) message (String)
 */
class PrivateMessageReader extends AbstractCommonReader<PrivateMessage> implements Reader<PrivateMessage>{
	

	private String message;
	private boolean readMsg = false;	

	/**
	 * process the bytebuffer to extract the sender login, the receiver login and the message 
	 * @param bb
	 * @return ProcessStatus of the state of the process
	 */
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readLogIn || !readReceiver || !readMsg) {
        	
        	ProcessStatus srState =  stringReader.process(bb);

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
        		readMsg = true;
        		message = stringReader.get();
        	}
    		stringReader.reset();
        }
        frame = new PrivateMessage(login, receiver, message);
        state=State.DONE;
        return ProcessStatus.DONE;
	}

	/**
	 * reset reader
	 */
	@Override
	public void reset() {
		super.reset();
		message = null;
		readMsg = false;

	}
	

	
}
