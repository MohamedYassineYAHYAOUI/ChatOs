package fr.uge.net.tcp.readers;

abstract class CommonMessageReader<T> {
	enum State {
		DONE, WAITING, ERROR
	};

	State state = State.WAITING;
	T message = null;
	String login = null;
	boolean readLogIn = false;
	boolean readMsg = false;
	
	final StringReader stringReader = new StringReader(); 

	T getMessage() {
		if (state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return message;
	}

	String getLogin() {
		if (state != State.DONE) {
			throw new IllegalStateException("Process not done");
		}
		return login;
	}

	void packetReset() {
		state = State.WAITING;
		stringReader.reset();
		message = null;
		readLogIn = false;
		readMsg = false;
	}

}
