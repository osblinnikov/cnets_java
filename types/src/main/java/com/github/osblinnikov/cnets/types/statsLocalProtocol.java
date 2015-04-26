package com.github.osblinnikov.cnets.types;

public class statsLocalProtocol {

  private long gridId;
  private int uniqueId;
  private int packets;
  private int bytes;
  private boolean writer;

  public void setGridId(long gridId) {
    this.gridId = gridId;
  }

  public long getGridId() {
    return gridId;
  }

  public void setUniqueId(int uniqueId) {
    this.uniqueId = uniqueId;
  }

  public int getUniqueId() {
    return uniqueId;
  }

  public void setPackets(int packets) {
    this.packets = packets;
  }

  public int getPackets() {
    return packets;
  }

  public void setBytes(int bytes) {
    this.bytes = bytes;
  }

  public int getBytes() {
    return bytes;
  }

  public void setWriter(boolean writer) {
    this.writer = writer;
  }

  public boolean isWriter() {
    return writer;
  }
}
