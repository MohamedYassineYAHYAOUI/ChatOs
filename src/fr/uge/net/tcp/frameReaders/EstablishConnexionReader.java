package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;


import fr.uge.net.tcp.visitor.Frame;




class EstablishConnexionReader<Q ,T extends Frame> implements Reader<T>{

	enum State {
		DONE, WAITING, ERROR
	};

	private State state = State.WAITING;
	private final Function<Q,T> function;
	private T establishConnection; 
	private Q id;
	private boolean readId = false;
	private final Reader<Q> longReader;

	
 	public EstablishConnexionReader(Reader<Q> reader, Function<Q,T> function ){
		this.longReader = Objects.requireNonNull(reader);
		this.function = Objects.requireNonNull(function);
	}

	@Override
	public ProcessStatus process(ByteBuffer bb) {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
        while(!readId) {
    		var srState = longReader.process(bb);
    		if (srState != ProcessStatus.DONE) {
    			return srState;
    		}
        	if(!readId) {
        		readId = true;
        		id = longReader.get();
        	}
        	longReader.reset();
        }
        state=State.DONE;
        establishConnection = function.apply(id); //new ServerConnection(login);
        return ProcessStatus.DONE;
		
	}
	@Override
	public T get() {
		if(state != State.DONE) {
			throw new IllegalStateException("EstablishConnexionReader");
		}
		return establishConnection;
	}
	@Override
	public void reset() {
		state = State.WAITING;
		establishConnection = null; 
		readId = false;
		longReader.reset();;
	}
	



}
