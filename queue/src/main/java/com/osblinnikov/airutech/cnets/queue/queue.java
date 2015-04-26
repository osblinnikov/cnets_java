
package com.github.osblinnikov.cnets.queue;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.types.*;
public class queue{
  long[] data;long maxIndex;long head;long tail;int capacity;
  
  public queue(int capacity){
    this.capacity = capacity;
    this.data = new long[capacity];
    onCreate();
    initialize();
  }

  private void initialize(){
    
    onKernels();
    
  }
/*[[[end]]] (1abd6d0e1b197d83eab863bdb8081ec5)*/

  private void onKernels() {

  }

  private void onCreate(){
    maxIndex = (long)(Long.MAX_VALUE/(long)capacity)*(long)capacity;
    head = tail = maxIndex - 5;
  }

  public boolean isEmpty() {
    return tail == head;
  }

  public boolean isFull() {
    long headMin = tail - (long)capacity;
    if(headMin < 0){
      headMin = maxIndex + headMin;
    }
    return (headMin == head);
  }

  public void enqueue(long obj) throws QueueFullException {
    if(isFull()){
      throw new QueueFullException("Queue is Full.");
    }else{
      tail++;
      if(tail >= maxIndex){tail = 0;}
      data[(int)(tail % capacity)] = obj;
    }
  }

  public long dequeue() throws QueueEmptyException {
    if(isEmpty()){
      throw new QueueEmptyException();
    }else{
      head++;
      if(head >= maxIndex){head = 0;}
      return data[(int)(head % capacity)];
    }
  }

  public int length(){
    int res;
    if(head <= tail){
      return (int)(tail - head);
    }else{
      res = (int)(maxIndex - head);
      res += tail;
      return res;
    }
  }
  public void clear(){
    head = tail = maxIndex - 5;
  }

}

