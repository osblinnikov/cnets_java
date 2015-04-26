
package com.github.osblinnikov.cnets.runnablesContainer;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

import com.github.osblinnikov.cnets.types.*;
public class runnablesContainer{
  
  public runnablesContainer(){
    onCreate();
    initialize();
  }

  private void initialize(){
    
    onKernels();
    
  }
/*[[[end]]] (checksum: 1968ccf977a05e85e2a42bd145b93ebd) (d3aa07db72ddfd84eb24c3a0afb195e6)*/

  private void onKernels() {

  }

  private void onCreate(){

  }

  private Kernel kernel = null;
  private runnablesContainer[] containers = null;
  private RunnableStoppable target = null;



  public void setContainers(runnablesContainer[] containers){
    this.containers = containers;
  }

/*getTarget and getContainers allow third-party libraries to implement launch-stop algorithms*/
  public RunnableStoppable getTarget(){return target;}
  public runnablesContainer[] getContainers(){if(target!=null){return new runnablesContainer[]{this};}else{return containers;}}
/**/

  public void setCore(RunnableStoppable target){
    this.target = target;
  }

  public void launch(boolean lockLastElement){
    if(target!=null){
      kernel = new Kernel();
      kernel.launch(target, lockLastElement);
    }else if(containers!=null){
      for(int i=0;i<containers.length - 1;i++){
        containers[i].launch(false);
      }
      containers[containers.length - 1].launch(lockLastElement);
    }
  }

  public void stop(){
    if(target!=null){
      if(kernel!=null) {
        kernel.stopThread();
        kernel = null;
      }
    }else if(containers!=null){
      for(int i=containers.length-1;i>=0;i--){
        containers[i].stop();
      }
    }
  }
}

