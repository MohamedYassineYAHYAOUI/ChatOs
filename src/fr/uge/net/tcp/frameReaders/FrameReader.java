package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;

import fr.uge.net.tcp.responses.Response.Codes;
import fr.uge.net.tcp.visitor.ConnectToServer;
import fr.uge.net.tcp.visitor.Frame;


public class FrameReader implements Reader<Frame> {
	
	private final IntReader intReader = new IntReader();
	private PublicMessageReader publicMessageReader = new PublicMessageReader(); // Trame: String - String
	private PrivateMessageReader privateMessageReader = new PrivateMessageReader(); // trame String - String - String 
	private PrivatConnexionRequestReader privatConnexionRequest = new PrivatConnexionRequestReader(); // Trame: String - String
	private PrivateConnexionRefusedReader privateConnexionRefused = new PrivateConnexionRefusedReader();  // Trame: String - String
	private PrivateConnexionAcceptedReader privateConnexionAcceptedReader = new PrivateConnexionAcceptedReader();  // Trame: String - String

	private Frame frame ;
	
	
	
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
	
	
	
	public Codes processCode() {
		var value = intReader.get();
		for (var code : Codes.values()) {
			if (code.getCode() == value) {
				return code;
			}
		}
		throw new IllegalArgumentException("invlaide packet code " + value);
	}
	

	
	 private ProcessStatus process(ByteBuffer bb, Reader<? extends Frame> reader ) {
			var res = reader.process(bb);
			if(res == ProcessStatus.DONE) {
				frame = reader.get();
				reader.reset();
			}
			return res;
	 }
	
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
				return process(bb, publicMessageReader);
			case PRIVATE_MESSAGE_RECEIVED:
				return process(bb, privateMessageReader);
			case REQUEST_PRIVATE_CONNEXION:
				return process(bb, privatConnexionRequest);
			case REFUSE_PRIVATE_CONNEXION:
				return process(bb, privateConnexionRefused);
			case ID_PRIVATE:
				return process(bb, privateConnexionAcceptedReader);
			default:
				throw new IllegalStateException("invalid opCode");
			}
		}catch(IllegalStateException e) {
			reset();
			return ProcessStatus.ERROR;
		}
	}

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
		//publicMessageReader.reset();
		
	}

}
