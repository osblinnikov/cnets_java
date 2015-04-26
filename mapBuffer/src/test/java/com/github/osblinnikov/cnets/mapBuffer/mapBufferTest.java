
package com.github.osblinnikov.cnets.mapBuffer;
import org.junit.Test;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/


import com.github.osblinnikov.cnets.types.*;
import com.github.osblinnikov.cnets.queue.*;
import com.github.osblinnikov.cnets.readerWriter.*;
/*[[[end]]] (checksum: 6a926588f849c3a13cc2351475932413) (3efbd5d22118764a838eba8c58dde9b6) */
public class mapBufferTest {
  @Test
  public void mapBufferTest(){
    Object buffers[] = new Object[1000];
    for(int i=0; i<buffers.length; i++){
      buffers[i] = new IntBoxer(10);
    }
    long timeout_milisec = 1000;
    int readers_grid_size = 1;
    mapBuffer m = new mapBuffer(buffers, timeout_milisec, readers_grid_size);
    reader r0 = m.getReader(0);
    final writer w0 = m.getWriter(0);

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
        writes_reads.writeFunction(w0,tries,false);
      }
    };
    writer.start();
    writes_reads.readFunction(r0,tries,false);
  }
}

