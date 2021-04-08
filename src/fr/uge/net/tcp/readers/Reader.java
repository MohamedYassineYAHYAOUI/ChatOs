package fr.uge.net.tcp.readers;

import java.nio.ByteBuffer;

 public interface Reader<T> {

    static enum ProcessStatus {DONE,REFILL,ERROR};

     ProcessStatus process(ByteBuffer bb);

     T get();

     void reset();

}