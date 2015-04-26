package com.github.osblinnikov.cnets.readerWriter;
import com.github.osblinnikov.cnets.types.*;


public class writer {
  private writer statsWriter = null;
  bufferKernelParams buffer = null;
  private long statsTime = System.currentTimeMillis();
  private int packetsCounter = 0;
  private int bytesCounter = 0;

  public writer(){buffer = new bufferKernelParams();}

  public writer(bufferKernelParams buffer){
    this.buffer = buffer;
  }

  public Object writeNext(int waitThreshold) {
    if(buffer == null){return null;}
    /*todo: add here special code for debuging data flow*/
    return buffer.getTarget().writeNext(buffer, waitThreshold);
  }

  public int writeFinished() {
    if (buffer == null) {
      return 0;
    }
    if (statsCollectorStatic.getStatsInterval() > 0) {
      if (statsWriter == null) {
        statsWriter = statsCollectorStatic.getWriter();
      }
      if (statsWriter != null && statsWriter.uniqueId() != uniqueId()) {
        packetsCounter++;
        long curTime = System.currentTimeMillis();
        if (curTime - statsTime > statsCollectorStatic.getStatsInterval()) {
          statsLocalProtocol p = (statsLocalProtocol) statsWriter.writeNext(0);
          if (p != null) {
            p.setWriter(true);
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
    return buffer.getTarget().writeFinished(buffer);
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

  public writer copy(){
    return new writer(buffer.copy());
  }

  public void incrementBytesCounter(int bytesCounter) {
    if (statsCollectorStatic.getStatsInterval() > 0) {
      this.bytesCounter += bytesCounter;
    }
  }
}