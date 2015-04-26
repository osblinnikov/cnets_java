package com.github.osblinnikov.cnets.types;

import com.github.osblinnikov.cnets.types.*;

public interface readerWriterInterface {
  public Object readNext(bufferKernelParams params, int waitThreshold);
  public bufferReadData readNextWithMeta(bufferKernelParams params, int waitThreshold);
  public int readFinished(bufferKernelParams params);
  public Object writeNext(bufferKernelParams params, int waitThreshold);
  public int writeFinished(bufferKernelParams params);
  public int size(bufferKernelParams params);
  public int timeout(bufferKernelParams params);
  public int gridSize(bufferKernelParams params);
  public int uniqueId(bufferKernelParams params);
  public int addSelector(bufferKernelParams params, Object selectorContainer);
}