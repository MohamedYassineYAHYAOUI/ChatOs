package fr.uge.net.tcp.server;

import java.nio.ByteBuffer;

interface Reader<T> {

    static enum ProcessStatus {DONE,REFILL,ERROR};

    public ProcessStatus process(ByteBuffer bb);

    public T get();

    public void reset();

}
