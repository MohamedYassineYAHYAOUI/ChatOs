package fr.uge.net.tcp.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import fr.uge.net.tcp.frameReaders.FrameReader;
import fr.uge.net.tcp.visitor.Frame;




class PublicContext extends CommonContext implements Context {
	
	private final ClientFrameVisitor frameVisitor;

	private final FrameReader frameReader;
	private boolean isConnected = false;
	private boolean canSendCommand = true;


	
	PublicContext(SelectionKey key, ProcessCommands inputProcess, ClientOS clientOs){
		super(key);

		this.frameVisitor = new ClientFrameVisitor(this, inputProcess, clientOs);
		this.frameReader = new FrameReader();
	}
	
	
	private void processIn() {
		for(;;) {
			var status = frameReader.process(bbin);
			switch(status) {
			case ERROR:
				silentlyClose();
				return;
			case REFILL:
				return;
			case DONE:
				Frame frame = frameReader.get();
				frameReader.reset();
				treatFrame(frame);
				break;
			}
		}
	}
	
	private void treatFrame(Frame frame) {
		frame.accept(frameVisitor);
	}
	
	
	
	/**
	 *  set if the client can't send new commands from the input 
	 * @param value true if user can send new commands, else false
	 */
	void setCanSendCommand(boolean value) {
		synchronized (queue) {
			canSendCommand = value;
		}
	}
	
    /**
     * @return true if client can send new commands from the input, else false
     */
	boolean canSendCommand() {
		synchronized (queue) {
			return canSendCommand;
		}
	}
	
	/**
	 * read bytes from sc and put them in bbin 
	 * The convention is that bbin is in write-mode before the call to process and
	 * after the call
	 * start process on bbin
	 */
	@Override
    public void doRead() throws IOException {
        if (sc.read(bbin) == -1) {
            closed = true;
        }
        processIn();
        updateInterestOps();
    }


	
	public void setConnected(boolean value) {
		isConnected = value;
		
	}
	
	public void setClosed(boolean value) {
		closed = value;
	}
	
	
	/**
	 * @return true if the context is connected to the server, else false
	 */
	boolean isConnected() {
		return isConnected;
	}
	
	
}
