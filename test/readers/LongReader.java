package fr.uge.net.tcp.readers;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import fr.uge.net.tcp.process.OpCodeProcess;
import fr.uge.net.tcp.process.Process;




public class LongReader implements Reader<Long>, Process{

	

    private enum State {DONE,WAITING,ERROR};
    private State state = State.WAITING;

    private final ByteBuffer internalbb = ByteBuffer.allocate(Long.BYTES); // write-mode
    private Long value;
	private OpCodeProcess opCodePorcess;
    private Consumer<Long> consumer;
    private boolean doneProcessing =false;
	
    public LongReader(OpCodeProcess opCodePorcess, Consumer<Long> consumer ) {
		this.opCodePorcess = opCodePorcess;
    	this.consumer = consumer;
    }
    
    public LongReader() {
		this(null, null);
    }
    
    
	@Override
	public boolean executeProcess(ByteBuffer bbin) {
		if(longProcess(bbin)) {
			consumer.accept(getId());
			reset();
			return true;
		}
		return false;
	}

    private boolean longProcess(ByteBuffer bbin) {
		Objects.requireNonNull(bbin);
		if (!doneProcessing) {
			switch (process(bbin)) {
			case DONE:
				doneProcessing = true;
				return true;
			case REFILL:
				return false;
			case ERROR:
				throw new IllegalStateException("error processing ID for client private connection");
			}
		}
		return true;
    }
	
	
	@Override
	public ProcessStatus process(ByteBuffer bb) {
        if (state== State.DONE || state== State.ERROR){
            throw new IllegalStateException();
        }
        bb.flip();
        try {
            if (bb.remaining()<=internalbb.remaining()){
                internalbb.put(bb);
            } else {
                var oldLimit = bb.limit();
                bb.limit(internalbb.remaining());
                internalbb.put(bb);
                bb.limit(oldLimit);
            }
        } finally {
            bb.compact();
        }
        
        if (internalbb.hasRemaining()){
            return ProcessStatus.REFILL;
        }
        
        
        state=State.DONE;
        internalbb.flip();
        value=internalbb.getLong();

        return ProcessStatus.DONE;
	}

	@Override
	public Long get() {
        if (state!= State.DONE) {
            throw new IllegalStateException();
        }
        return value;
	}

	@Override
	public void reset() {
        state= State.WAITING;
        internalbb.clear();
        if(opCodePorcess != null) {
            opCodePorcess.reset();
            doneProcessing = false;
        }

	}

	@Override
	public String getLogin() {
		throw new UnsupportedOperationException("operation not valide for Long reader");
	}

	@Override
	public String getMessage() {
		throw new UnsupportedOperationException("operation not valide for Long reader");
	}

	@Override
	public long getId() {
        if (state!= State.DONE) {
            throw new IllegalStateException();
        }
        return value;
	}

	@Override
	public String getTargetLogin() {
		throw new UnsupportedOperationException("operation not valide for Long reader");
	}


}
