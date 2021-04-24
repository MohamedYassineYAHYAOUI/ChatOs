package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;


import fr.uge.net.tcp.visitor.Frame;


/**
 * Reader for packets of the type T, such as 
 * LOGIN_PRIVATE: login (String)
 * REQUEST_SERVER_CONNECTION: ID (Long)
 * DISCONNECT_PRIVATE: login (String)
 *
 * @param <Q> type of the value (Long , String ...)
 * @param <T> generic type for the frame
 */

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

 	/**
 	 * process the bytebuffer
 	 */
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
	/**
	 * get the frame value if the state is Done
	 */
	@Override
	public T get() {
		if(state != State.DONE) {
			throw new IllegalStateException("EstablishConnexionReader");
		}
		return establishConnection;
	}
	
	/**
	 * reset reader
	 */
	@Override
	public void reset() {
		state = State.WAITING;
		establishConnection = null; 
		readId = false;
		longReader.reset();;
	}
	



}
