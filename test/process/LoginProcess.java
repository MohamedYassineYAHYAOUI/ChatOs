package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import fr.uge.net.tcp.readers.StringReader;

/**
 * process buffers that contains :
 * Format : 	( String )
 * 
 * Example :
 * LOGIN :  login (STRING)
 */

public class LoginProcess implements Process  {

	
	private boolean doneProcessing = false;
	final private StringReader stringReader = new StringReader();
	private final Consumer<String> toConsume; //consummer for the operation to execute
	private final OpCodeProcess opCodePorcess;
	

	public LoginProcess(OpCodeProcess opCodePorcess, Consumer<String > toConsume) {
		this.opCodePorcess = Objects.requireNonNull(opCodePorcess);
		this.toConsume = Objects.requireNonNull(toConsume);
	}
	
	
	/**
	 * execute the process passed to login Process if the reader operation is valid 
	 */
	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if(process(bbin)) {
			toConsume.accept(getLogin());
			reset();
			return true;
		}
		return false;
	}
	
	/**
	 * process the bbin using the stringReader reader
	 * @param bbin buffer to process
	 * @return ProcessStatus  of the reader
	 */
	private boolean process(ByteBuffer bbin) {
		if (!doneProcessing) {
			switch (stringReader.process(bbin)) {
			case DONE:
				doneProcessing = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing Login for client ");
			}
		}
		return true;
	}

	/**
	 * get the login in the buffer
	 */
	@Override
	public String getLogin() {
		return stringReader.get();
	}

	@Override
	public String getTargetLogin() {
		throw new UnsupportedOperationException("operation not valide for LoginProcess");
	}

	
	/**
	 * reset object
	 */
	@Override
	public void reset() {
		doneProcessing = false;
		stringReader.reset();
		opCodePorcess.reset();
	}

	@Override
	public String getMessage() {
		throw new UnsupportedOperationException("operation not valide for LoginProcess");
	}

	@Override
	public long getId() {
		throw new UnsupportedOperationException("operation not valide for LoginProcess");
	}



}
