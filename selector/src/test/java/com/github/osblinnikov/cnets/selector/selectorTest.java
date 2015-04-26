
package com.github.osblinnikov.cnets.selector;
import org.junit.Test;
/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/


import com.github.osblinnikov.cnets.types.*;
import com.github.osblinnikov.cnets.runnablesContainer.*;
import com.github.osblinnikov.cnets.queue.*;
import com.github.osblinnikov.cnets.readerWriter.*;
import com.github.osblinnikov.cnets.mapBuffer.*;
/*[[[end]]] (checksum: c0b239af56f3ed1aa444271283f06e70) (4e9b3ce1b2cee064f740e6f18f6e21d2)  */

class intBoxerWriter implements RunnableStoppable{
  writer w0;
  intBoxerWriter(writer w0){
    this.w0 = w0;
  }

  public runnablesContainer getRunnables(){

    runnablesContainer runnables = new runnablesContainer();
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
  reader r0;
  intBoxerReader(reader r0){
    this.r0 = r0;
  }

  public runnablesContainer getRunnables(){

    runnablesContainer runnables = new runnablesContainer();
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
  public void selectorTest(){
    int bufSizes = 1000;
        /*Buffer for Data*/
    Object buffers[] = new Object[bufSizes];
    for(int i=0; i<buffers.length; i++){
      buffers[i] = new IntBoxer(10);
    }
    long timeout_milisec = 1000;
    int readers_grid_size = 1;
    mapBuffer m = new mapBuffer(buffers, timeout_milisec, readers_grid_size);
    reader r0 = m.getReader(0);
    final writer w0 = m.getWriter(0);
    selector classObj = new selector(new reader[]{r0});
    intBoxerReader readerKernel = new intBoxerReader(classObj.getReader(0,-1));
    intBoxerWriter writerKernel = new intBoxerWriter(w0);
        /*running kernels*/
    runnablesContainer runnables = new runnablesContainer();
    runnablesContainer[] arrContainers = new runnablesContainer[2];
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

