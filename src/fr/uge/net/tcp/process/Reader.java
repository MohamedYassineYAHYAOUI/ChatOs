package fr.uge.net.tcp.process;

import java.nio.ByteBuffer;

 interface Reader<T> {

    static enum ProcessStatus {DONE,REFILL,ERROR};

     ProcessStatus process(ByteBuffer bb);

     T get();

     void reset();

}
