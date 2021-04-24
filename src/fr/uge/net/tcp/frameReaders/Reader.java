package fr.uge.net.tcp.frameReaders;

import java.nio.ByteBuffer;


 interface Reader<T> {

    static enum ProcessStatus {DONE,REFILL,ERROR};

    /**
     * process the ByteBuffer bb to extract values
     * @param bb ByteBuffer to process
     * @return ProcessStatus of the state of the byteBuffer
     */
     ProcessStatus process(ByteBuffer bb);

     /**
      * get the value in the ByteBuffer of type T
      * @return value in ByteBufer after processing
      */
     T get();

     /**
      * reset the byteBuffer
      */
     void reset();

}
