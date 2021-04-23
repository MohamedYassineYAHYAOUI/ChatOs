package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;

import fr.uge.net.tcp.visitor.Frame;


public class PrivateConnexionReader<T extends Frame> extends AbstractCommonReader<T> implements Reader<T> {


	private final BiFunction<String, String , T> function;

	public PrivateConnexionReader(BiFunction<String, String , T> function) {
		this.function = Objects.requireNonNull(function);
	}


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

	@Override
	public void reset() {
		super.reset();
	}



}
