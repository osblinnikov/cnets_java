package com.github.osblinnikov.cnetsjava.readerwriter;

public class LinkedContainer {
  private LinkedContainer next = null;
  private LinkedContainer prev = null;
  private final Writer w;
  private final Reader r;

  public LinkedContainer(Writer w, Reader r){
    this.w = w; this.r = r;
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
    w.getBuffer().getTarget().writeFinished(w.getBuffer());
    if(next!=null){
      next.call();
    }
  }

  public void reverseCall() {
    r.getBuffer().getTarget().readFinished(r.getBuffer());
    if(next != null){
      next.reverseCall();
    }
  }
}