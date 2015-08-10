
package com.github.osblinnikov.cnetsjava.mapbuffer;
import org.junit.Test;

import com.github.osblinnikov.cnetsjava.types.*;
import com.github.osblinnikov.cnetsjava.readerwriter.*;

public class mapBufferTest {
  @Test
  public void testMapBuffer(){
    Object buffers[] = new Object[1000];
    for(int i=0; i<buffers.length; i++){
      buffers[i] = new IntBoxer(10);
    }
    long timeout_milisec = 1000;
    int readers_grid_size = 1;
    MapBuffer m = new MapBuffer(buffers, timeout_milisec, readers_grid_size);
    Reader r0 = m.getReader(0);
    final Writer w0 = m.getWriter(0);

    /***INLINE TEST**/
    IntBoxer res = (IntBoxer)w0.writeNext(-1);
    if(res == null){throw new  NullPointerException();}
    res.value = 100;
    if(0!=w0.writeFinished()){throw new NullPointerException();}

    IntBoxer resRead = (IntBoxer)r0.readNext(-1);
    if(resRead == null){throw new  NullPointerException();}
    System.out.println(resRead.value);
    if(0!=r0.readFinished()){throw new NullPointerException();}

    /***THREADING TEST**/
    final int tries = 10000000;
    Thread writer = new Thread(){
      @Override
      public void run() {
        WritesReads.writeFunction(w0, tries, false);
      }
    };
    writer.start();
    WritesReads.readFunction(r0, tries, false);
  }
}

