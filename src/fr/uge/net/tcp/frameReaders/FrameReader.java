package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.responses.Response.Codes;
import fr.uge.net.tcp.visitor.ConnectToServer;
import fr.uge.net.tcp.visitor.DisconnectFromServer;
import fr.uge.net.tcp.visitor.EstablishConnexion;
import fr.uge.net.tcp.visitor.Frame;
import fr.uge.net.tcp.visitor.PrivateConnexionAccepted;
import fr.uge.net.tcp.visitor.PrivateConnexionRequest;
import fr.uge.net.tcp.visitor.PrivateConnexionRefused;
import fr.uge.net.tcp.visitor.ServerConnection;


public class FrameReader implements Reader<Frame> {
	
	private final IntReader intReader = new IntReader();

	private Frame frame ;

	/**
	 * process byteBuffer bb 
	 * @param bb Bytebuffer to process
	 * @return true if the byteBuffer is Done, false if the process in refill
	 * @throws IllegalStateException if the process return ERROR
	 */
	public boolean processOpCode(ByteBuffer bb) {
		switch(intReader.process(bb)) {
		case DONE:
			return true;
		case REFILL:
			return false;
		case ERROR:
			throw new IllegalStateException("error processing code for client ");
		default:
			return false;
		}
	}

	/**
	 * process the code passed in the start of the packet
	 * @return Codes coresponding to the packet
	 * @throws IllegalArgumentException if Codes.values doesn't contain the value
	 */
	public Codes processCode() {
		var value = intReader.get();
		for (var code : Codes.values()) {
			if (code.getCode() == value) {
				return code;
			}
		}
		throw new IllegalArgumentException("invlaide packet code " + value);
	}
	

	/**
	 * process bytebuffer using the Reader reader
	 * @param bb bytebuffer to process 
	 * @param reader Reader to process the byteBuffer with 
	 * @return ProcessStatus  to reflect the current state of the process
	 */
	 private ProcessStatus process(ByteBuffer bb, Reader<? extends Frame> reader ) {
			var res = reader.process(bb);
			if(res == ProcessStatus.DONE) {
				frame = reader.get();
				reader.reset();
			}
			return res;
	 }
	 
	 /**
	  * a byte buffer in write-mode, parse the opCode at the begging of buffer 
	  * and determine the action to apply on the rest of the buffer
	  * Convention is bb in write-mode before and after this method
	  * @param bb ByteBuffer to process
	  * @return ProcessStatus status of the action applied on the buffer
	  */
	@Override
	public ProcessStatus process(ByteBuffer bb) {
		try {
			if(!processOpCode(bb)) {
				return ProcessStatus.REFILL;
			}
			switch(processCode()) {
			case LOGIN_ACCEPTED:
				frame = new ConnectToServer(true);
				return ProcessStatus.DONE;
			case LOGIN_REFUSED:
				frame = new ConnectToServer(false);
				return ProcessStatus.DONE;
			case PUBLIC_MESSAGE_RECEIVED:
				return process(bb, new PublicMessageReader());
			case PRIVATE_MESSAGE_RECEIVED:
				return process(bb, new PrivateMessageReader());
			case REQUEST_PRIVATE_CONNEXION:
				return process(bb, new PrivateConnexionReader<>((requester, target)->{return new PrivateConnexionRequest((String) requester, (String) target);}));
			case REFUSE_PRIVATE_CONNEXION:
				return process(bb, new PrivateConnexionReader<>((requester, target)->{return new PrivateConnexionRefused((String) requester, (String) target);}));
			case ID_PRIVATE:
				return process(bb, new PrivateConnexionAcceptedReader());
			case REQUEST_SERVER_CONNECTION:
				return process(bb, new EstablishConnexionReader<>(new StringReader(), (login) ->{return new ServerConnection((String)login);}));
			case PUBLIC_MESSAGE_SENT:
				return process(bb, new PublicMessageReader());
			case PRIVATE_MESSAGE_SENT:
				return process(bb, new PrivateMessageReader());
			case ACCEPT_PRIVATE_CONNEXION:
				return process(bb, new PrivateConnexionReader<>((requester, target)->{return new PrivateConnexionAccepted((String) requester, (String) target);}));
			case LOGIN_PRIVATE:
				return process(bb, new EstablishConnexionReader<>(new LongReader(), (id) ->{return new EstablishConnexion((Long)id);}));
			case DISCONNECT_PRIVATE:
				return process(bb, new EstablishConnexionReader<>(new LongReader(), (id) ->{return new DisconnectFromServer((Long)id);}));
			default:
				throw new IllegalStateException("invalid opCode");
			}
		}catch(IllegalStateException e) {
			reset();
			return ProcessStatus.ERROR;
		}
	}	

	/**
	 * get the Frame 
	 * @return Frame to apply after parsing the buffer
	 * @throws IllegalStateException if the opcode doesn't match an operation
	 */
	@Override
	public Frame get() {
		if(frame == null) {
			throw new IllegalStateException("Frame is null");
		}
		return frame;
	}

	@Override
	public void reset() {
		frame = null;
		intReader.reset();
	}

}
