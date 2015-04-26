
package com.github.osblinnikov.cnets.selector;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.types.*;
import com.github.osblinnikov.cnets.runnablesContainer.*;
import com.github.osblinnikov.cnets.queue.*;
import com.github.osblinnikov.cnets.readerWriter.*;
import com.github.osblinnikov.cnets.mapBuffer.*;

class selectorContainer{
  int bufferId;
}
public class selector implements readerWriterInterface{
  reader[] reducableReaders;
  
  public selector(reader[] reducableReaders){
    this.reducableReaders = reducableReaders;
    onCreate();
    initialize();
  }


public reader getReader(long gridId,int bufferId){
  Object container = null;
  selectorContainer obj = new selectorContainer();
  obj.bufferId = bufferId;
  container = obj;
  return new reader(new bufferKernelParams(this, gridId, container));
}
public writer getWriter(long gridId,int bufferId){
  Object container = null;
  selectorContainer obj = new selectorContainer();
  obj.bufferId = bufferId;
  container = obj;
  return new writer(new bufferKernelParams(this, gridId, container));
}
  private void initialize(){
    
    onKernels();
    
  }
  
/*[[[end]]] (checksum: 99c5599fedd8e417279c12210b12ddcd) (4bf3df2c047c757a70ec42f7686a6ace) */
  linkedContainer[] allContainers;
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
    allContainers = new linkedContainer[reducableReaders.length];
    for(int i=0; i<reducableReaders.length; i++){
      if(reducableReaders[i] == null){
        allContainers[i] = null;
        continue;
      }
      allContainers[i] = new linkedContainer(getWriter(i,-1));
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
  public Object readNext(bufferKernelParams params, int waitThreshold) {
    return readNextWithMeta(params, waitThreshold).getData();
  }

  @Override
  public bufferReadData readNextWithMeta(bufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    selector m = (selector)params.getTarget();
    bufferReadData res = new bufferReadData();
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
    }while(writesToContainers[lastReadId] == 0);
    sumWrites--;
    writesToContainers[lastReadId]--;
    switch_cv_lock.unlock();
    res = reducableReaders[lastReadId].readNextWithMeta(0);
    if(res.getData() != null){
      res.setNested_buffer_id(lastReadId);
      selectorContainer container = (selectorContainer)params.getAdditionalData();
      if(container!=null){
        container.bufferId = lastReadId;
      }
    }
    return res;
  }

  @Override
  public int readFinished(bufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
    int res = -1;
    if(m==null){
      System.err.println("ERROR: selector readFinished: Some Input parameters are wrong");
      return res;
    }

    selectorContainer container = (selectorContainer)params.getAdditionalData();
    if(container!=null && container.bufferId >=0 && container.bufferId < reducableReaders.length){
      res = reducableReaders[container.bufferId].readFinished();
      container.bufferId = -1;
    }else{
      System.err.println("ERROR: selector readFinished: some params are wrong");
    }
    return res;
  }

  @Override
  public Object writeNext(bufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    selector m = (selector)params.getTarget();
    Object res = null;
    if(m==null){
      System.err.println("ERROR: selector writeNext: Some Input parameters are wrong");
      return res;
    }
    System.err.println("ERROR: selector writeNext is not allowed");
    return res;
  }

  @Override
  public int writeFinished(bufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
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
  public int size(bufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector size: Some Input parameters are wrong");
      return -1;
    };
    selectorContainer container = (selectorContainer)params.getAdditionalData();
    if(container==null || container.bufferId <0 || container.bufferId >= reducableReaders.length){
      return -1;
    }
    return reducableReaders[container.bufferId].size();
  }

  @Override
  public int timeout(bufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector timeout: Some Input parameters are wrong");
      return -1;
    };
    return timeout_milisec;
  }

  @Override
  public int gridSize(bufferKernelParams params){
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector gridSize: Some Input parameters are wrong");
      return -1;
    };
    return 1;
  }

  @Override
  public int uniqueId(bufferKernelParams params){return -1;}

  @Override
  public int addSelector(bufferKernelParams params, Object selectorContainer) {
    if(this != params.getTarget()){return -1;}
    selector m = (selector)params.getTarget();
    if(m==null){
      System.err.println("ERROR: selector addSelector: Some Input parameters are wrong");
      return -1;
    };
    System.err.println("ERROR: selector addSelector: not allowed operation");
    return -1;
  }
}

