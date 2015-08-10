package com.github.osblinnikov.cnetsjava.runnablescontainer;

import com.github.osblinnikov.cnetsjava.readerwriter.Reader;

public interface RunnableStoppable{
  public void onStart();
  public void run();
  public void onStop();
  public void onDestroy();
  public Reader[] getReaders();
}