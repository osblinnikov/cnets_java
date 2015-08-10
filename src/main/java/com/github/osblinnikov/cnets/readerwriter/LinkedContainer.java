package com.github.osblinnikov.cnets.readerwriter;

public class LinkedContainer {
  private LinkedContainer next = null;
  private LinkedContainer prev = null;

  private Writer w;

  public LinkedContainer(Writer w){
    this.w = w;
  }

  public void add(LinkedContainer added){
    if(this.next!=null){
        this.next.add(added);
    }else {
        this.next = added;
        added.prev = this;
    }
  }
  public void remove(){
    if(prev != null){
      prev.next = next;
    }
    if(next!=null){        
      next.prev = prev;
    }
  }
  public void call(){
    w.writeFinished();
    if(next!=null){
      next.call();
    }
  }
}