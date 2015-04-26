
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
/*[[[end]]] (checksum: af01440617c3cf9f655475d250eb18ae) (af01440617c3cf9f655475d250eb18ae)*/

  private void onCreate(){

  }

  private void onKernels(){

  }


}

