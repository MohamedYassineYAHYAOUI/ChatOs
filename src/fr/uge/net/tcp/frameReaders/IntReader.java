package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;


/**
 * Reader for Integer values 
 */
class IntReader implements Reader<Integer> {

    private enum State {DONE,WAITING,ERROR};

    private State state = State.WAITING;
    private final ByteBuffer internalbb = ByteBuffer.allocate(Integer.BYTES); // write-mode
    private int value;

    /**
	 * process the byteBuffer and extract the Integer value
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
        value=internalbb.getInt();

        return ProcessStatus.DONE;
    }

    /**
     * Reader for Integer values 
     */
    @Override
    public Integer get() {
        if (state!= State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    /**
     * reset Reader
     */
    @Override
    public void reset() {
        state= State.WAITING;
        internalbb.clear();
    }
}
