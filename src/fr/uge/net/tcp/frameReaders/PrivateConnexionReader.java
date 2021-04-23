package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;

import fr.uge.net.tcp.visitor.Frame;


public class PrivateConnexionReader<T extends Frame> implements Reader<T> {

	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private final BiFunction<String, String , T> function;
	private T privateConnexion; 
	private String requester;
	private String target;
	private boolean readRequester = false;
	private boolean readTarget = false;
	private final StringReader stringReader = new StringReader(); 
	
	
	
	public PrivateConnexionReader(BiFunction<String, String , T> function) {
		this.function = Objects.requireNonNull(function);
	}


	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readRequester || !readTarget) {
        	
    		var srState = stringReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readRequester) {
        		readRequester = true;
        		requester = stringReader.get();

        	}else {
        		readTarget = true;
        		target = stringReader.get();
        		stringReader.reset();	
        	}
    		stringReader.reset();
        	
        }
        state=State.DONE;
        privateConnexion = function.apply(requester, target);
        return ProcessStatus.DONE;
	}
	
	
	@Override
	public T get() {
		if(state != State.DONE) {
			throw new IllegalStateException("State is not Done");
		}
		return privateConnexion;
	}

	@Override
	public void reset() {
		state = State.WAITING;
		
		privateConnexion = null; 
		requester = null;
		target = null;
		readRequester = false;
		readTarget = false;
		stringReader.reset(); 
	}



}
