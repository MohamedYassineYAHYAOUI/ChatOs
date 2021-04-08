package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.uge.net.tcp.readers.PrivateMessageReader;
import fr.uge.net.tcp.readers.Reader;

/**
 * process buffers that contains :
 * Format : 	( String ) - ( String ) - ( T )
 * 
 * @param <T> the type of the value in the buffer
 *  String to process  MESSAGE_PRIVATE  : login_requester (STRING) login_target (STRING) msg (STRING)
 *  Long to process    ID_PRIVATE  		: login_requester (STRING) login_target (STRING) connect_id (LONG)
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

	/**
	 * execute the process passed to generic Value if the reader operation is valide 
	 */
	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if (process(bbin)) {
			toExecte.accept(getLogin(),getTargetLogin(), getValue()); // opearion o execute
			reset(); //reset object
			return true;
		}
		return false;
	}
	
	/**
	 * process the bbin using the privateMessage reader
	 * @param bbin buffer to process
	 * @return ProcessStatus  of the reader
	 */
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

	/**
	 * get the login of in the buffer
	 */
	@Override
	public String getLogin() {
		return pvmessageReader.getSenderLogin();
	}

	/**
	 * get the value in the buffer
	 * @return
	 */
	public T getValue() {
		return pvmessageReader.getMessage();
	}
	
	/**
	 * get the target in buffer
	 */
	@Override
	public String getTargetLogin() {
		return pvmessageReader.getTargetLogin();
	}

	/**
	 * reset object
	 */
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
