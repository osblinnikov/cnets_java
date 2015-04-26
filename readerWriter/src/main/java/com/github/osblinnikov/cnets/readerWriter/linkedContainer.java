package com.github.osblinnikov.cnets.readerWriter;

public class linkedContainer{
  private linkedContainer next = null;
  private linkedContainer prev = null;

  private writer w;

  public linkedContainer(writer w){
    this.w = w;
  }

  public void add(linkedContainer added){
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