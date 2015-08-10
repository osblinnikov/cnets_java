package com.github.osblinnikov.cnetsjava.readerwriter;

public interface ReaderWriterInterface {
  public Object readNext(BufferKernelParams params, int waitThreshold);
  public BufferReadData readNextWithMeta(BufferKernelParams params, int waitThreshold);
  public int readFinished(BufferKernelParams params);
  public Object writeNext(BufferKernelParams params, int waitThreshold);
  public int writeFinished(BufferKernelParams params);
  public int writeFinishedWithMeta(BufferKernelParams params, BufferWriteData writeData);
  public int size(BufferKernelParams params);
  public int timeout(BufferKernelParams params);
  public int gridSize(BufferKernelParams params);
  public int uniqueId(BufferKernelParams params);
  public int addSelector(BufferKernelParams params, Object selectorContainer);
  public int enable(BufferKernelParams params, boolean isEnabled);
}