
package com.github.osblinnikov.cnets.mapbuffer;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.readerwriter.*;

public class MapBuffer implements ReaderWriterInterface {
  Object[] buffers;long timeout_milisec;int readers_grid_size;
  
  public MapBuffer(Object[] buffers, long timeout_milisec, int readers_grid_size){
    this.buffers = buffers;
    this.timeout_milisec = timeout_milisec;
    this.readers_grid_size = readers_grid_size;
    onCreate();
    initialize();
  }


public Reader getReader(long gridId){
  Object container = null;
  return new Reader(new BufferKernelParams(this, gridId, container));
}
public Writer getWriter(long gridId){
  Object container = null;
  return new Writer(new BufferKernelParams(this, gridId, container));
}
  private void initialize(){
    
    onKernels();
    
  }
  
/*[[[end]]] (checksum: ddc577011cafb4fa2baf984e00aaf3fa) (28926fd84afa051f0bffc6691b831551) */
  private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
  private final Lock fReadLock = fLock.readLock();
  private final Lock fWriteLock = fLock.writeLock();
  private volatile LinkedContainer selectorContainers = null;
  private final Lock              switch_cv_lock = new ReentrantLock();
  private final Condition switch_cv  = switch_cv_lock.newCondition();
  private final Lock free_buffers_cv_mutex = new ReentrantLock();
  private final Condition         free_buffers_cv  = free_buffers_cv_mutex.newCondition();
  private final Lock free_buffers_queue_mutex = new ReentrantLock();
  private Boolean[] isEnabled;
  private ArrayDeque<Integer> free_buffers;
  private Integer[] buffers_grid_ids;
  private AtomicInteger[] buffers_to_read;
  private ArrayDeque<Integer>[] grid;
  private Lock[] grid_lock;
  private int uniqueId;

  private void onKernels() {

  }

  private void onCreate(){
    this.uniqueId = StatsCollectorStatic.getNextLocalId();
    this.free_buffers = new ArrayDeque<Integer>(buffers.length);
    this.buffers_grid_ids = new Integer[buffers.length];
    this.buffers_to_read = new AtomicInteger[buffers.length];
    this.isEnabled = new Boolean[readers_grid_size];
    this.grid = new ArrayDeque[readers_grid_size];
    this.grid_lock = new Lock[readers_grid_size];
    for(int i=0;i<readers_grid_size;i++){
      isEnabled[i] = true;
      grid[i] = new ArrayDeque<Integer>(buffers.length);
      grid_lock[i] = new ReentrantLock();
    }
    for(int i = 0; i < buffers.length; i++){
      buffers_to_read[i] = new AtomicInteger(0);
      free_buffers.add(i);
    }
  }

  @Override
  public Object readNext(BufferKernelParams params, int waitThreshold) {
    return readNextWithMeta(params, waitThreshold).getData();
  }

  @Override
  public BufferReadData readNextWithMeta(BufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    MapBuffer m = (MapBuffer)params.getTarget();
    BufferReadData res = new BufferReadData();
    if(m==null || grid.length == 0 || grid.length <= params.getGrid_id()){
      System.err.println("ERROR: mapbuffer "+uniqueId+" readNextWithMeta: Some Input parameters are wrong");
      System.err.println("ERROR: mapbuffer: m==null ("+m+") || grid.length == 0 ("+grid.length+") || grid.length <= params.getGrid_id()  ("+params.getGrid_id()+")");
      return res;
    }

    if(!isEnabled[((int) params.getGrid_id())]){
      return res;
    }

    long nanosec = (waitThreshold < 0)? timeout_milisec*1000000L : waitThreshold*1000000L;

    /*find the reader's queue*/
    ArrayDeque gridQ = grid[(int)params.getGrid_id()];
    Lock grid_mutex = grid_lock[(int)params.getGrid_id()];
    /*Lock `wrote` dqueue for the Reader "params.getGrid_id()"*/

    /*if number of wrote dqueue elements = 0*/
    if(nanosec > 0){
      grid_mutex.lock();
      if(gridQ.isEmpty()) {
        /*wait until cond variable of wrote buffer with "Lock `wrote` dqueue" mutex*/
        grid_mutex.unlock();
        switch_cv_lock.lock();
        grid_mutex.lock();
        if (gridQ.isEmpty()) {
          grid_mutex.unlock();
          do {
            try {
              nanosec = switch_cv.awaitNanos(nanosec);
            } catch (InterruptedException e) {
              nanosec = 0;
            }
            grid_mutex.lock();
            boolean isEmpty = gridQ.isEmpty();
            grid_mutex.unlock();
            if (!isEmpty) {
              break;
            }
          } while (nanosec > 0);
          grid_mutex.lock();
        }
        switch_cv_lock.unlock();
      }
    }else{
      grid_mutex.lock();
    }

    if(!gridQ.isEmpty()){
      /*write id of buffer to q->internalId variable or NULL if no one buffer available*/
      params.setInternalId((Integer) gridQ.pop());
      grid_mutex.unlock();
      if(params.getInternalId() >= 0 && params.getInternalId() < buffers.length ){
        res.setData(buffers[params.getInternalId()]);
        res.setWriter_grid_id(buffers_grid_ids[params.getInternalId()]);
      }
    }else{
      grid_mutex.unlock();
    }
    return res;
  }

  @Override
  public int readFinished(BufferKernelParams params) {
    if(this != params.getTarget()){return -1;}
    MapBuffer m = (MapBuffer)params.getTarget();
    if(m==null || grid.length == 0 || grid.length <= params.getGrid_id() || buffers.length <= params.getInternalId()){
      System.err.println("ERROR: mapbuffer "+uniqueId+" readFinished: Some Input parameters are wrong");
      return -1;
    }

    if(buffers_to_read[params.getInternalId()].decrementAndGet() > 0){
      return 0;
    }
    /*here, we are only in case if read everything*/

    free_buffers_queue_mutex.lock();
    free_buffers.add(params.getInternalId());
    free_buffers_queue_mutex.unlock();

    free_buffers_cv_mutex.lock();
    free_buffers_cv.signal();
    free_buffers_cv_mutex.unlock();
    return 0;
  }

  @Override
  public Object writeNext(BufferKernelParams params, int waitThreshold) {
    if(this != params.getTarget()){return null;}
    MapBuffer m = (MapBuffer)params.getTarget();
    if(m==null || grid.length == 0){
      System.err.println("ERROR: mapbuffer "+uniqueId+" writeNext: Some Input parameters are wrong");
      return null;
    }
    Object res = null;
    long nanosec = (waitThreshold < 0)? timeout_milisec*1000000L : waitThreshold*1000000L;

    if(nanosec>0){
      free_buffers_queue_mutex.lock();
      if(free_buffers.isEmpty()) {
        free_buffers_queue_mutex.unlock();
        free_buffers_cv_mutex.lock();
        free_buffers_queue_mutex.lock();
        if(free_buffers.isEmpty()) {
          free_buffers_queue_mutex.unlock();
          do {
            try {
              nanosec = free_buffers_cv.awaitNanos(nanosec);
            } catch (InterruptedException e) {
              nanosec = 0;
            }
            free_buffers_queue_mutex.lock();
            boolean isEmpty = free_buffers.isEmpty();
            free_buffers_queue_mutex.unlock();
            if (!isEmpty) {
              break;
            }
          } while (nanosec > 0);
          free_buffers_queue_mutex.lock();
        }
        free_buffers_cv_mutex.unlock();
      }
    }else{
      free_buffers_queue_mutex.lock();
    }
    if(!free_buffers.isEmpty()){
      params.setInternalId(free_buffers.pop());
      free_buffers_queue_mutex.unlock();
      res = buffers[params.getInternalId()];
    }else{
      free_buffers_queue_mutex.unlock();
    }
    return res;
  }

  public int writeFinishedWithMeta(BufferKernelParams params, BufferWriteData writeData) {
    if (this != params.getTarget()) {
      return -1;
    }
    MapBuffer m = (MapBuffer) params.getTarget();
    if (m == null || grid.length == 0 || buffers.length <= params.getInternalId()) {
      System.err.println("ERROR: mapbuffer " + uniqueId + " writeFinished: Some Input parameters are wrong");
      return -1;
    }
    if (buffers_to_read[params.getInternalId()].get() > 0) {
      System.err.printf("ERROR: mapbuffer " + uniqueId + " writeFinished: ERROR not all readers read buffer %d, there are %d remain!\n", params.getInternalId(), buffers_to_read[params.getInternalId()].get());
      return -1;
    }
    int readers_size = 0;
    int grdId = (int) params.getGrid_id();
    int internalId = params.getInternalId();
    if (writeData == null || writeData.getGrid_ids() == null || writeData.getGrid_ids().length == 0) {
      for (int i = 0; i < readers_grid_size; ++i) {
        if (!isEnabled[i]) {
          continue;
        }
        buffers_to_read[internalId].incrementAndGet();
        grid_lock[i].lock();
        grid[i].add(internalId);
        buffers_grid_ids[internalId] = grdId;
        grid_lock[i].unlock();
        ++readers_size;
      }
    } else {
      int[] gridIds = writeData.getGrid_ids();
      for (int gridId : gridIds) {
        int i = gridId % readers_grid_size;
        if (!isEnabled[i]) {
          continue;
        }
        buffers_to_read[internalId].incrementAndGet();
        grid_lock[i].lock();
        grid[i].add(internalId);
        buffers_grid_ids[internalId] = grdId;
        grid_lock[i].unlock();
        ++readers_size;
      }
    }


    if (readers_size > 0){
      switch_cv_lock.lock();
      switch_cv.signalAll();
      switch_cv_lock.unlock();

      fReadLock.lock();
      try {
        if (selectorContainers != null) {
          selectorContainers.call();
        }
      } finally {
        fReadLock.unlock();
      }
    }
    return 0;
  }

  @Override
  public int writeFinished(BufferKernelParams params) {
    return writeFinishedWithMeta(params, null);
  }

  @Override
  public int size(BufferKernelParams params){
    return buffers.length;
  }

  @Override
  public int timeout(BufferKernelParams params){
    return (int)timeout_milisec;
  }

  @Override
  public int gridSize(BufferKernelParams params){
    return readers_grid_size;
  }

  @Override
  public int uniqueId(BufferKernelParams params){
    return uniqueId;
  }

  @Override
  public int addSelector(BufferKernelParams params, Object selectorContainer) {
    LinkedContainer sContainer = (LinkedContainer)selectorContainer;
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

