
package com.github.osblinnikov.cnets.selector;
import org.junit.Test;

import com.github.osblinnikov.cnets.types.*;
import com.github.osblinnikov.cnets.runnablescontainer.*;
import com.github.osblinnikov.cnets.readerwriter.*;
import com.github.osblinnikov.cnets.mapbuffer.*;

class intBoxerWriter implements RunnableStoppable{
  Writer w0;
  intBoxerWriter(Writer w0){
    this.w0 = w0;
  }

  public RunnablesContainer getRunnables(){

    RunnablesContainer runnables = new RunnablesContainer();
    runnables.setCore(this);
    return runnables;
  }
  int packIterator = 0, lastSecPack = 0;
  long curtime = System.currentTimeMillis();
  long endtime_sec = curtime + 1000;/*1sec*/

  @Override
  public void onStart() {

  }

  @Override
  public void run(){
    IntBoxer d = (IntBoxer) w0.writeNext(-1);
    if (d != null) {
      d.value = packIterator;
      w0.writeFinished();
      packIterator++;
      curtime = System.currentTimeMillis();
      if (endtime_sec <= curtime) {
        endtime_sec = curtime + 1000;
        System.out.printf("w thread FPS: %d\n", packIterator - lastSecPack);
        lastSecPack = packIterator;
      }
    }
  }

  @Override
  public void onStop() {

  }
}


class intBoxerReader implements RunnableStoppable{
  Reader r0;
  intBoxerReader(Reader r0){
    this.r0 = r0;
  }

  public RunnablesContainer getRunnables(){

    RunnablesContainer runnables = new RunnablesContainer();
    runnables.setCore(this);
    return runnables;
  }

  int packIterator = 0, lastSecPack = 0;
  long curtime = System.currentTimeMillis();
  long endtime_sec = curtime + 1000;/*1sec*/

  @Override
  public void onStart() {

  }

  @Override
  public void run(){
    IntBoxer d = (IntBoxer) r0.readNext(-1);
    if (d != null) {
      r0.readFinished();
      packIterator++;
      curtime = System.currentTimeMillis();
      if (endtime_sec <= curtime) {
        endtime_sec = curtime + 1000;
        System.out.printf("r thread FPS: %d\n", packIterator - lastSecPack);
        lastSecPack = packIterator;
      }
    }
  }

  @Override
  public void onStop() {

  }
}

public class selectorTest {
  @Test
  public void testSelector(){
    int bufSizes = 1000;
        /*Buffer for Data*/
    Object buffers[] = new Object[bufSizes];
    for(int i=0; i<buffers.length; i++){
      buffers[i] = new IntBoxer(10);
    }
    long timeout_milisec = 1000;
    int readers_grid_size = 1;
    MapBuffer m = new MapBuffer(buffers, timeout_milisec, readers_grid_size);
    Reader r0 = m.getReader(0);
    final Writer w0 = m.getWriter(0);
    Selector classObj = new Selector(new Reader[]{r0});
    intBoxerReader readerKernel = new intBoxerReader(classObj.getReader(0,-1));
    intBoxerWriter writerKernel = new intBoxerWriter(w0);
        /*running kernels*/
    RunnablesContainer runnables = new RunnablesContainer();
    RunnablesContainer[] arrContainers = new RunnablesContainer[2];
    arrContainers[0] = readerKernel.getRunnables();
    arrContainers[1] = writerKernel.getRunnables();
    runnables.setContainers(arrContainers);
    runnables.launch(false);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    runnables.stop();
  }
}

