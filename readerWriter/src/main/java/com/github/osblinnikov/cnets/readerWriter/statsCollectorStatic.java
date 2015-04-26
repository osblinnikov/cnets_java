package com.github.osblinnikov.cnets.readerWriter;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class statsCollectorStatic {
  private volatile static writer w = null;

  public static void setWriter(writer wIn){
    if(w==null) {
      w = wIn;
    }else{
      System.err.printf("WARN: statsCollectorStatic: setWriter: writer already set\n");
    }
  }

  public static writer getWriter(){
    if(w!=null) {
      return w.copy();
    }else{
      return null;
    }
  }

  private static AtomicInteger localId = new AtomicInteger(0);
  public static int getNextLocalId(){
    return localId.getAndIncrement();
  }

  private static AtomicLong statsInterval = new AtomicLong(0L);

  public static void setStatsInterval(long i){statsInterval.set(i);}
  public static long getStatsInterval() {return statsInterval.get();}
}
