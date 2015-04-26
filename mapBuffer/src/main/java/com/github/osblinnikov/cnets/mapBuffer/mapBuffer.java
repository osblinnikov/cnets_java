
package com.github.osblinnikov.cnets.mapBuffer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.types.*;
import com.github.osblinnikov.cnets.queue.*;
import com.github.osblinnikov.cnets.readerWriter.*;

public class mapBuffer implements readerWriterInterface{
  Object[] buffers;long timeout_milisec;int readers_grid_size;
  
  public mapBuffer(Object[] buffers,long timeout_milisec,int readers_grid_size){
    this.buffers = buffers;
    this.timeout_milisec = timeout_milisec;
    this.readers_grid_size = readers_grid_size;
    onCreate();
    initialize();
  }


public reader getReader(long gridId){
  Object container = null;
  return new reader(new bufferKernelParams(this, gridId, container));
}
public writer getWriter(long gridId){
  Object container = null;
  return new writer(new bufferKernelParams(this, gridId, container));
}
  private void initialize(){
    
    onKernels();
    
  }
  
/*[[[end]]] (checksum: ddc577011cafb4fa2baf984e00aaf3fa) (28926fd84afa051f0bffc6691b831551) */
  private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
  private final Lock fReadLock = fLock.readLock();
  private final Lock fWriteLock = fLock.writeLock();
  private linkedContainer selectorContainers = null;
  private final Lock              switch_cv_lock = new ReentrantLock();
  private final Condition switch_cv  = switch_cv_lock.newCondition();
  private final Lock              free_buffers_cv_lock = new ReentrantLock();
  private final Condition         free_buffers_cv  = free_buffers_cv_lock.newCondition();
  private final Lock              free_buffers_cv_ow = new ReentrantLock();
  private queue free_buffers;
  private Integer[] buffers_grid_ids;
  private AtomicInteger[] buffers_to_read;
  private queue[] grid;
  private Lock[] grid_lock;
  private int uniqueId;

  private void onKernels() {

  }

  private void onCreate(){
    this.uniqueId = statsCollectorStatic.getNextLocalId();
    this.free_buffers = new queue(buffers.length);
    this.buffers_grid_ids = new Integer[buffers.length];
    this.buffers_to_read = new AtomicInteger[buffers.length];
    this.grid = new queue[readers_grid_size];
    this.grid_lock = new Lock[readers_grid_size];
    for(int i=0;i<readers_grid_size;i++){
      grid[i] = new queue(buffers.length);
      grid_lock[i] = new ReentrantLock();
    }
    for(int i = 0; i < buffers.length; i++){
      buffers_to_read[i] = new AtomicInteger(0);
      try {
        free_buffers.enqueue((long)i);
      } catch (QueueFullException e) {
        e.printStackTrace();
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
    mapBuffer m = (mapBuffer)params.getTarget();
    bufferReadData res = new bufferReadData();
    if(m==null || grid.length == 0 || grid.length <= params.getGrid_id()){
      System.err.println("ERROR: mapBuffer "+uniqueId+" readNextWithMeta: Some Input parameters are wrong");
      System.err.println("ERROR: mapBuffer: m==null ("+m+") || grid.length == 0 ("+grid.length+") || grid.length <= params.getGrid_id()  ("+params.getGrid_id()+")");
      return res;
    }
    long nanosec = 0;
    if(waitThreshold < 0){
      nanosec = timeout_milisec*1000000L;
    }else if(waitThreshold > 0){
      nanosec = waitThreshold*1000000L;
    }

    /*find the reader's queue*/
    queue gridQ = grid[(int)params.getGrid_id()];
    Lock grid_mutex = grid_lock[(int)params.getGrid_id()];
    /*Lock `wrote` dqueue for the Reader "params.getGrid_id()"*/
    grid_mutex.lock();

    /*if number of wrote dqueue elements = 0*/
    if(nanosec > 0 && gridQ.isEmpty()){
      /*wait until cond variable of wrote buffer with "Lock `wrote` dqueue" mutex*/
      grid_mutex.unlock();
      switch_cv_lock.lock();
      grid_mutex.lock();
      if(gridQ.isEmpty()){
        do{
          grid_mutex.unlock();
          try {
            nanosec = switch_cv.awaitNanos(nanosec);
          } catch (InterruptedException e) {
            nanosec = 0;
          }
//          if(nanosec <= 0){
//            System.out.println("WARN: mapBuffer "+uniqueId+" readNextWithMeta: Wait timeout, params.getGrid_id()='"+params.getGrid_id());
//          }
          grid_mutex.lock();
          if(!gridQ.isEmpty()){
            break;
          }
        }while(nanosec > 0);
      }
      switch_cv_lock.unlock();
    }

    if(!gridQ.isEmpty()){
      /*write id of buffer to q->internalId variable or NULL if no one buffer available*/
      try {
        params.setInternalId((int)gridQ.dequeue());
        if(params.getInternalId() >= 0 && params.getInternalId() < buffers.length ){
          res.setData(buffers[params.getInternalId()]);
          res.setWriter_grid_id(buffers_grid_ids[params.getInternalId()]);
        }
      } catch (QueueEmptyException e) {
        System.err.println("ERROR: mapBuffer "+uniqueId+" readNextWithMeta: Dequeue is failed");
        e.printStackTrace();
      }
    }
    /*Unlock `wrote` dqueue for the Reader "params.getGrid_id()"*/
    grid_mutex.unlock();
    return res;
  }

  @Override
  public int readFinished(bufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    mapBuffer m = (mapBuffer)params.getTarget();
    if(m==null || grid.length == 0 || grid.length <= params.getGrid_id() || buffers.length <= params.getInternalId()){
      System.err.println("ERROR: mapBuffer "+uniqueId+" readFinished: Some Input parameters are wrong");
      return -1;
    }
    if(buffers_to_read[params.getInternalId()].decrementAndGet() > 0){
      return 0;
    }
    /*here, we are only in case if read everything*/
    free_buffers_cv_lock.lock();
    try {
      free_buffers.enqueue((long)params.getInternalId());
    } catch (QueueFullException e) {
      System.err.println("ERROR: mapBuffer "+uniqueId+" readFinished: Enqueue failed"+params.getInternalId());
      e.printStackTrace();
    }
    free_buffers_cv.signal();
    free_buffers_cv_lock.unlock();
    return 0;
  }

  @Override
  public Object writeNext(bufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    mapBuffer m = (mapBuffer)params.getTarget();
    Object res = null;
    if(m==null || grid.length == 0){
      System.err.println("ERROR: mapBuffer "+uniqueId+" writeNext: Some Input parameters are wrong");
      return res;
    }
    long nanosec = 0;
    if(waitThreshold < 0){
      nanosec = timeout_milisec*1000000L;
    }else if(waitThreshold > 0){
      nanosec = waitThreshold*1000000L;
    }
    free_buffers_cv_ow.lock();
    free_buffers_cv_lock.lock();
    if(nanosec>0 && free_buffers.isEmpty()){
      /*Lock uniq Publisher - only one Publisher will wait for condition variable
      wait until cond variable of free buffer with "Lock `free` stack" mutex*/
      try {
        nanosec = free_buffers_cv.awaitNanos(nanosec);
      } catch (InterruptedException e) {
        nanosec = 0;
      }
      if(nanosec == 0){
        System.out.println("WARN: mapBuffer "+uniqueId+" writeNext: Wait timeout, params.getGrid_id()='"+params.getGrid_id());
      }
    }
    if(!free_buffers.isEmpty()){
      try {
        params.setInternalId((int)free_buffers.dequeue());
        res = buffers[params.getInternalId()];
      } catch (QueueEmptyException e) {
        System.err.println("ERROR: mapBuffer "+uniqueId+" writeNext: Dequeue is failed");
        e.printStackTrace();
      }
    }
    free_buffers_cv_lock.unlock();
    free_buffers_cv_ow.unlock();
    return res;
  }

  @Override
  public int writeFinished(bufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    mapBuffer m = (mapBuffer)params.getTarget();
    if(m==null || grid.length == 0 || buffers.length <= params.getInternalId()){
      System.err.println("ERROR: mapBuffer "+uniqueId+" writeFinished: Some Input parameters are wrong");
      return -1;
    };
    if(buffers_to_read[params.getInternalId()].get() > 0){
      System.err.printf("ERROR: mapBuffer "+uniqueId+" writeFinished: ERROR not all readers read buffer %d, there are %d remain!\n", params.getInternalId(), buffers_to_read[params.getInternalId()].get());
      return -1;
    }
    buffers_to_read[params.getInternalId()].set(grid.length);
    for(int i=0; i<grid.length; ++i){
      grid_lock[i].lock();
      try {
        grid[i].enqueue((long)params.getInternalId());
        buffers_grid_ids[params.getInternalId()] = (int)params.getGrid_id();
      } catch (QueueFullException e) {
        System.err.printf("ERROR: mapBuffer "+uniqueId+" writeFinished: Enqueue %d failed\n", params.getInternalId());
        e.printStackTrace();
      }

      grid_lock[i].unlock();
    }
    switch_cv_lock.lock();
    switch_cv.signalAll();
    switch_cv_lock.unlock();

    fReadLock.lock();
    try {
      if(selectorContainers!=null){
        selectorContainers.call();
      }
    }finally{
      fReadLock.unlock();
    }
    return 0;
  }

  @Override
  public int size(bufferKernelParams params){
    return buffers.length;
  }

  @Override
  public int timeout(bufferKernelParams params){
    return (int)timeout_milisec;
  }

  @Override
  public int gridSize(bufferKernelParams params){
    return readers_grid_size;
  }

  @Override
  public int uniqueId(bufferKernelParams params){
    return uniqueId;
  }

  @Override
  public int addSelector(bufferKernelParams params, Object selectorContainer) {
    linkedContainer sContainer = (linkedContainer)selectorContainer;
    fWriteLock.lock();
    try {
      if(selectorContainers == null){selectorContainers = sContainer;}
      else{selectorContainers.add(sContainer);}
    }finally{
      fWriteLock.unlock();
    }
    return 0;
  }
}

