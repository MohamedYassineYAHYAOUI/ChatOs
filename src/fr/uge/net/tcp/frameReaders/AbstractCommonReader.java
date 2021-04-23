package fr.uge.net.tcp.frameReaders;


abstract class AbstractCommonReader<T> {
	
	enum State {
		DONE, WAITING, ERROR
	};

	State state = State.WAITING;
	T frame;
	String login;
	String  receiver;
	boolean readLogIn = false;
	boolean readReceiver = false;
	final StringReader stringReader = new StringReader();

	public T get() {
		if( state !=  State.DONE) {
			throw new IllegalStateException("State is not Done");
		}
		return frame;
	}

	void reset() {
		frame = null;
		state = State.WAITING;
		login = null;
		receiver = null;
		readLogIn = false;
		readReceiver = false;
		stringReader.reset();
	}
	
	
}
