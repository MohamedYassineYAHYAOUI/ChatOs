package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;

import fr.uge.net.tcp.visitor.Frame;

/**
 * Reader to process packets of type Login (String) receiver (String)
 * @param <T> generic type that extends Frame
 * this reader can work on the packets like
 *  REQUEST_PRIVATE_CONNEXION, REFUSE_PRIVATE_CONNEXION, ACCEPT_PRIVATE_CONNEXION
 */
class PrivateConnexionReader<T extends Frame> extends AbstractCommonReader<T> implements Reader<T> {


	private final BiFunction<String, String , T> function;

	public PrivateConnexionReader(BiFunction<String, String , T> function) {
		this.function = Objects.requireNonNull(function);
	}

	/**
	 * process the byteBuffer and extract information
	 */
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
        frame = function.apply(login, receiver);
        return ProcessStatus.DONE;
	}

	/**
	 * reset reader
	 */
	@Override
	public void reset() {
		super.reset();
	}



}
