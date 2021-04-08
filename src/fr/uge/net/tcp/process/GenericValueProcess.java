package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.uge.net.tcp.readers.PrivateMessageReader;
import fr.uge.net.tcp.readers.Reader;

/**
 * 
 *  String - String - T
 *  T : Long, String 
 *  String - String - String -> (STRING) (STRING) msg (STRING)
 *  String - String - Long 	 -> (STRING) (STRING) connect_id (LONG)
 * @param <T>
 */

public class GenericValueProcess<T> implements Process {

	private boolean doneProcessing = false;
	private final PrivateMessageReader<T> pvmessageReader;
	private final OpCodeProcess opCodePorcess;
	private final TriConsumer<String, String, T> toExecte;

	public GenericValueProcess(OpCodeProcess opCodePorcess, Reader<T> packetReader,
			TriConsumer<String, String, T> toExecte) {

		Objects.requireNonNull(packetReader);
		this.opCodePorcess = Objects.requireNonNull(opCodePorcess);
		this.toExecte = Objects.requireNonNull(toExecte);
		this.pvmessageReader = new PrivateMessageReader<T>(packetReader);
	}

	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if (process(bbin)) {
			toExecte.accept(getLogin(),getTargetLogin(), getValue());
			reset();
			return true;
		}
		return false;
	}

	private boolean process(ByteBuffer bbin) {
		Objects.requireNonNull(bbin);
		if (!doneProcessing) {
			switch (pvmessageReader.process(bbin)) {
			case DONE:
				doneProcessing = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing privte packet for client ");
			}
		}
		return true;
	}

	@Override
	public String getLogin() {
		return pvmessageReader.getSenderLogin();
	}

	public T getValue() {
		return pvmessageReader.getMessage();
	}

	@Override
	public String getTargetLogin() {
		return pvmessageReader.getTargetLogin();
	}

	@Override
	public void reset() {
		doneProcessing = false;
		opCodePorcess.reset();
		pvmessageReader.reset();
	}

	@Override
	public String getMessage() {
		throw new UnsupportedOperationException("use getValue()");

	}

	@Override
	public long getId() {
		throw new UnsupportedOperationException("operation not valide for geniric Value Process");
	}

}
