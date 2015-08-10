package com.github.osblinnikov.cnets.readerwriter;
import com.github.osblinnikov.cnets.types.*;


public class Reader {


  private final BufferKernelParams buffer;

  private Writer statsWriter = null;
  private String kernelPath = null;
  private int packetsCounter = 0;
  private int bytesCounter = 0;
  private long statsTime = System.currentTimeMillis();

  public Reader(){buffer = new BufferKernelParams();}

  public Reader(BufferKernelParams buffer){
    this.buffer = buffer;
  }

  public BufferReadData readNextWithMeta(int waitThreshold) {
    if(buffer == null){return new BufferReadData();}
    /*todo: add here special code for debugging data flow*/
    return buffer.getTarget().readNextWithMeta(buffer, waitThreshold);
  }

  public Object readNext(int waitThreshold) {
    if(buffer == null){return 0;}
    /*todo: add here special code for debugging data flow*/
    return buffer.getTarget().readNext(buffer, waitThreshold);
  }

  public int readFinished() {
    if (buffer == null) {
      return 0;
    }
    /*todo: add here special code for debugging data flow*/
    if(StatsCollectorStatic.getStatsInterval() > 0) {
      if (statsWriter == null) {
        statsWriter = StatsCollectorStatic.getWriter();
      }
      if (statsWriter != null && statsWriter.uniqueId() != uniqueId()) {
        packetsCounter++;
        long curTime = System.currentTimeMillis();
        if (curTime - statsTime > StatsCollectorStatic.getStatsInterval()) {
          StatsLocalProtocol p = (StatsLocalProtocol) statsWriter.writeNext(0);
          if (p != null) {
            p.setWriter(false);
            p.setUniqueId(uniqueId());
            p.setGridId(buffer.getGrid_id());
            p.setPackets(packetsCounter);
            p.setBytes(bytesCounter);
            statsWriter.writeFinished();
            statsTime = curTime;
            packetsCounter = 0;
            bytesCounter = 0;
          }
        }
      }
    }
    return buffer.getTarget().readFinished(buffer);
  }

  public int size(){
    if(buffer == null){return 0;}
    return buffer.getTarget().size(buffer);
  }

  public int timeout(){
    if(buffer == null){return 0;}
    return buffer.getTarget().timeout(buffer);
  }
  
  public int gridSize(){
    if(buffer == null){return 0;}
    return buffer.getTarget().gridSize(buffer); 
  }

  public int uniqueId(){
    if(buffer == null){return -1;}
    return buffer.getTarget().uniqueId(buffer);
  }

  public int addSelector(LinkedContainer container){
    if(buffer == null){return -1;}
    return buffer.getTarget().addSelector(buffer, (Object)container);
  }

  public String getKernelPath() {
    return kernelPath;
  }

  public void setKernelPath(String kernelPath) {
    this.kernelPath = kernelPath;
  }

  public void incrementBytesCounter(int bytesCounter) {
    if (StatsCollectorStatic.getStatsInterval() > 0) {
      this.bytesCounter += bytesCounter;
    }
  }

  public Reader copy() {
    return new Reader(buffer.copy());
  }
}