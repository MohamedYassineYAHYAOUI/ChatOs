package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiConsumer;

import fr.uge.net.tcp.readers.MessageReader;


/**
 * Process buffers that contains :
 * Format :  	( String ) - ( String )
 * 
 */

public class MessageProcess implements Process {

	private boolean doneProcessing = false;
	private final MessageReader messageReader;
	private final OpCodeProcess opCodePorcess;
	private final BiConsumer<String, String > toExecte;

	public MessageProcess(OpCodeProcess opCodePorcess, BiConsumer<String, String> toExecte) {
		this.opCodePorcess = Objects.requireNonNull(opCodePorcess);
		this.messageReader = new MessageReader();
		this.toExecte = Objects.requireNonNull(toExecte);
	}
	
	/**
	 * execute the process passed to MessageProcess if the reader operation is valid 
	 */
	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if (process(bbin)) {
			toExecte.accept(getLogin(), getMessage());
			reset();
			return true;
		}
		return false;
	}

	/**
	 * process the bbin using the messageReader reader
	 * @param bbin buffer to process
	 * @return ProcessStatus  of the reader
	 */
	 private boolean process(ByteBuffer bbin) {
		Objects.requireNonNull(bbin);
		if (!doneProcessing) {
			switch (messageReader.process(bbin)) {
			case DONE:
				doneProcessing = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing packet for client ");
			}
		}
		return true;
	}

	@Override
	public String getLogin() {
		return messageReader.getLogin();
	}

	
	public String getValue() {
		throw new UnsupportedOperationException("operation not valide for Message Process");
	}

	@Override
	public String getTargetLogin() {
		return messageReader.getMessage();
	}

	@Override
	public void reset() {
		opCodePorcess.reset();
		messageReader.reset();
		doneProcessing = false;
	}

	@Override
	public String getMessage() {
		return messageReader.getMessage();
	}

	@Override
	public long getId() {
		throw new UnsupportedOperationException("operation not valide for Message Process");
	}

}
