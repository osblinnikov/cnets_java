package com.github.osblinnikov.cnets.runnablescontainer;

public interface RunnableStoppable{
  public void onStart();
  public void run();
  public void onStop();
}