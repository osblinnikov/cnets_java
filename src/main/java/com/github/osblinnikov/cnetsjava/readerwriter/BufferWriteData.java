package com.github.osblinnikov.cnetsjava.readerwriter;

public class BufferWriteData {
  public int[] getGrid_ids() {
    return grid_ids;
  }

  public void setGrid_ids(int[] grid_ids) {
    this.grid_ids = grid_ids;
  }

  public boolean isFromNetwork() {
    return isFromNetwork;
  }

  public void setIsFromNetwork(boolean isFromNetwork) {
    this.isFromNetwork = isFromNetwork;
  }

  int[] grid_ids;
  boolean isFromNetwork;
}
