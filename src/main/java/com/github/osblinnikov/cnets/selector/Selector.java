
package com.github.osblinnikov.cnets.selector;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.readerwriter.*;

class SelectorContainer {
  int bufferId;
}
public class Selector implements ReaderWriterInterface {
  Reader[] reducableReaders;
  
  public Selector(Reader[] reducableReaders){
    this.reducableReaders = reducableReaders;
    onCreate();
    initialize();
  }


public Reader getReader(long gridId,int bufferId){
  Object container = null;
  SelectorContainer obj = new SelectorContainer();
  obj.bufferId = bufferId;
  container = obj;
  return new Reader(new BufferKernelParams(this, gridId, container));
}
public Writer getWriter(long gridId,int bufferId){
  Object container = null;
  SelectorContainer obj = new SelectorContainer();
  obj.bufferId = bufferId;
  container = obj;
  return new Writer(new BufferKernelParams(this, gridId, container));
}
  private void initialize(){
    
    onKernels();
    
  }
  
/*[[[end]]] (checksum: 99c5599fedd8e417279c12210b12ddcd) (4bf3df2c047c757a70ec42f7686a6ace) */
  LinkedContainer[] allContainers;
  int[] writesToContainers;
  int timeout_milisec = Integer.MAX_VALUE;
  int lastReadId = -1;
  int sumWrites = 0;
  final Lock switch_cv_lock = new ReentrantLock();
  final Condition switch_cv  = switch_cv_lock.newCondition();

  private void onKernels() {

  }

  private void onCreate(){
    writesToContainers = new int[reducableReaders.length];
    allContainers = new LinkedContainer[reducableReaders.length];
    for(int i=0; i<reducableReaders.length; i++){
      if(reducableReaders[i] == null){
        allContainers[i] = null;
        continue;
      }
      allContainers[i] = new LinkedContainer(getWriter(i,-1));
      if(0!=reducableReaders[i].addSelector(allContainers[i])){
        System.err.println("selectorConstruct addSelector failed\n");
      }
      if(timeout_milisec>reducableReaders[i].timeout()){
        timeout_milisec = reducableReaders[i].timeout();
      }
    }
  }

  public void onDestroy(){
    for(int i=0; i<reducableReaders.length; i++){
      if(allContainers[i] != null) {
        allContainers[i].remove();
      }
    }
  }

  @Override
  public Object readNext(BufferKernelParams params, int waitThreshold) {
    return readNextWithMeta(params, waitThreshold).getData();
  }

  @Override
  public BufferReadData readNextWithMeta(BufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    Selector m = (Selector)params.getTarget();
    BufferReadData res = new BufferReadData();
    if(m==null){
      System.err.println("ERROR: selector readNextWithMeta: Some Input parameters are wrong");
      return res;
    }
    long nanosec = 0;
    if(waitThreshold < 0){
      nanosec = timeout_milisec*1000000L;
    }else if(waitThreshold > 0){
      nanosec = waitThreshold*1000000L;
    }
    switch_cv_lock.lock();
    if(sumWrites <= 0 && nanosec > 0){
      do{
        try {
          nanosec = switch_cv.awaitNanos(nanosec);
        } catch (InterruptedException e) {
          nanosec = 0;
        }
        if(sumWrites > 0){
          break;
        }else if(nanosec <= 0){
          switch_cv_lock.unlock();
          return res;
        }
      }while(nanosec > 0);
    }
    do{
      ++lastReadId;
      if(lastReadId >= reducableReaders.length){lastReadId = 0;}
    }while(writesToContainers[lastReadId] == 0 && sumWrites > 0);
    sumWrites--;
    writesToContainers[lastReadId]--;
    switch_cv_lock.unlock();
    res = reducableReaders[lastReadId].readNextWithMeta(0);
    if(res.getData() != null){
      res.setNested_buffer_id(lastReadId);
      SelectorContainer container = (SelectorContainer)params.getAdditionalData();
      if(container!=null){
        container.bufferId = lastReadId;
      }
    }
    return res;
  }

  @Override
  public int readFinished(BufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    int res = -1;
    if(m==null){
      System.err.println("ERROR: selector readFinished: Some Input parameters are wrong");
      return res;
    }

    SelectorContainer container = (SelectorContainer)params.getAdditionalData();
    if(container!=null && container.bufferId >=0 && container.bufferId < reducableReaders.length){
      res = reducableReaders[container.bufferId].readFinished();
      container.bufferId = -1;
    }else{
      System.err.println("ERROR: selector readFinished: some params are wrong");
    }
    return res;
  }

  @Override
  public Object writeNext(BufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    Selector m = (Selector)params.getTarget();
    Object res = null;
    if(m==null){
      System.err.println("ERROR: selector writeNext: Some Input parameters are wrong");
      return res;
    }
    System.err.println("ERROR: selector writeNext is not allowed");
    return res;
  }

  @Override
  public int writeFinished(BufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector writeFinished: Some Input parameters are wrong");
      return -1;
    };
    switch_cv_lock.lock();
    sumWrites++;
    writesToContainers[(int)params.getGrid_id()]++;
    switch_cv.signalAll();
    switch_cv_lock.unlock();
    return 0;
  }

  @Override
  public int size(BufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector size: Some Input parameters are wrong");
      return -1;
    };
    SelectorContainer container = (SelectorContainer)params.getAdditionalData();
    if(container==null || container.bufferId <0 || container.bufferId >= reducableReaders.length){
      return -1;
    }
    return reducableReaders[container.bufferId].size();
  }

  @Override
  public int timeout(BufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector timeout: Some Input parameters are wrong");
      return -1;
    };
    return timeout_milisec;
  }

  @Override
  public int gridSize(BufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector gridSize: Some Input parameters are wrong");
      return -1;
    };
    return 1;
  }

  @Override
  public int uniqueId(BufferKernelParams params){return -1;}

  @Override
  public int addSelector(BufferKernelParams params, Object selectorContainer) {
    if(this != params.getTarget()){return -1;}
    Selector m = (Selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector addSelector: Some Input parameters are wrong");
      return -1;
    };
    System.err.println("ERROR: selector addSelector: not allowed operation");
    return -1;
  }
}

