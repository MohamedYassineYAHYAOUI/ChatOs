package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Objects;
import java.util.function.Consumer;



/**
 * process Buffer with the format : String
 */

public class LoginProcess implements ProcessInt  {

	
	private boolean doneProcessing = false;
	final private StringReader stringReader = new StringReader();
	private final Consumer<String> toConsume;
	private final OpCodeProcess opCodePorcess;
	

	public LoginProcess(OpCodeProcess opCodePorcess, Consumer<String > toConsume) {
		this.opCodePorcess = Objects.requireNonNull(opCodePorcess);
		this.toConsume = Objects.requireNonNull(toConsume);
	}
	
	
	
	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if(process(bbin)) {
			toConsume.accept(getLogin());
			reset();
			return true;
		}
		return false;
	}
	
	
	
	
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
	
	@Override
	public String getLogin() {
		return stringReader.get();
	}


	@Override
	public String getTargetLogin() {
		throw new UnsupportedOperationException("operation not valide for LoginProcess");
	}

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
