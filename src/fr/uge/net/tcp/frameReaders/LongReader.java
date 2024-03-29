package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;


/**
 * Reader for Long values 
 */
class LongReader implements Reader<Long>{

	

    private enum State {DONE,WAITING,ERROR};
    private State state = State.WAITING;

    private final ByteBuffer internalbb = ByteBuffer.allocate(Long.BYTES); // write-mode
    private Long value;

	/**
	 * process the byteBuffer and extract the long value
	 */
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

	/**
	 * @return return long value
	 */
	@Override
	public Long get() {
        if (state!= State.DONE) {
            throw new IllegalStateException();
        }
        return value;
	}

	/**
	 * reset reader
	 */
	@Override
	public void reset() {
        state= State.WAITING;
        internalbb.clear();
	}



}
