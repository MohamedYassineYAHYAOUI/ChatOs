package fr.uge.net.tcp.reader;

import java.nio.ByteBuffer;

public interface Reader<T> {

    static enum ProcessStatus {DONE,REFILL,ERROR};

    public ProcessStatus process(ByteBuffer bb);

    public T get();

    public void reset();

}
