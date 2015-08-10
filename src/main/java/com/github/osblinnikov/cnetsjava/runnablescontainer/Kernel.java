package com.github.osblinnikov.cnetsjava.runnablescontainer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Kernel extends Thread{
  private RunnableStoppable objectToRun;
  private Boolean isSeparateThread = false;
  private AtomicBoolean isRunning = new AtomicBoolean(false);
  private AtomicBoolean stopFlag = new AtomicBoolean(false);

  private final Lock isRunning_cv_lock = new ReentrantLock();
  private final Condition isRunning_cv  = isRunning_cv_lock.newCondition();

  public void launch(RunnableStoppable objectToRun, boolean lockLaunch){
    stopFlag.set(false);
    if(!isRunning.getAndSet(true)){
      isRunning_cv_lock.lock();
      isRunning_cv.signalAll();
      isRunning_cv_lock.unlock();

      this.objectToRun = objectToRun;
//      objectToRun.onStart();
      if(lockLaunch){
        isSeparateThread = false;
        this.run();
      }else{
        isSeparateThread = true;
        this.start();
      }
    }
  }

  public void stopThread(){
    stopFlag.set(true);
    if(isSeparateThread) {
      this.interrupt();
    }
    while(isRunning.get()){
      stopFlag.set(true);/*make sure that nobody will start the kernel before we finish the waiting*/
//      try{Thread.sleep(1);}catch(InterruptedException e){e.printStackTrace();}
      isRunning_cv_lock.lock();
      try {isRunning_cv.await(1, TimeUnit.SECONDS);} catch (InterruptedException e) {e.printStackTrace();}
      isRunning_cv_lock.unlock();
    }
    try {
      if (isSeparateThread) {
        this.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
//    objectToRun.onStop();
  }

  @Override
  public void run(){
    while(!stopFlag.get()){
        try{
            objectToRun.run();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    isRunning.set(false);
    isRunning_cv_lock.lock();
    isRunning_cv.signalAll();
    isRunning_cv_lock.unlock();
  }

}