
    package com.github.osblinnikov.cnets.queue;
    import com.github.osblinnikov.cnets.queue.queue;
/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/


import com.github.osblinnikov.cnets.types.*;
/*[[[end]]] (checksum: 789f6c3d0e965f811cf44b477f2c60b3) (789f6c3d0e965f811cf44b477f2c60b3)*/
public class main{
  public static void main(String[] args){
    queue classObj = new queue(1);
    runnablesContainer runnables = classObj.getRunnables();
    runnables.launch(true);
    
  }
}
