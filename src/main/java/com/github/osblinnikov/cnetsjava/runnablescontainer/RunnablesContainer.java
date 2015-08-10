
package com.github.osblinnikov.cnetsjava.runnablescontainer;

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile))
]]]*/

public class RunnablesContainer {
  
  public RunnablesContainer(){
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
  private RunnablesContainer[] containers = null;
  private RunnableStoppable target = null;
  private String targetName = "";
  private int spawnMode = 0;


  public void setContainers(RunnablesContainer[] containers){
    this.containers = containers;
  }

/*getTarget and getContainers allow third-party libraries to implement launch-stop algorithms*/
  public RunnableStoppable getTarget(){return target;}
  public RunnablesContainer[] getContainers(){if(target!=null){return new RunnablesContainer[]{this};}else{return containers;}}
/**/

  public void setCore(RunnableStoppable target, String targetName, int spawnMode){
    this.target = target;
    this.targetName = targetName;
    this.spawnMode = spawnMode;
  }

  public void launch(boolean lockLastElement){
    doLaunch(lockLastElement, true);
    doLaunch(lockLastElement, false);
  }

  private void doLaunch(boolean lockLastElement, boolean isOnStart) {
    if(containers!=null){
      for(int i=0;i<containers.length;i++){
        if(isOnStart && target != null){
          System.out.printf("=> launch: %s mode=%d\n",targetName,spawnMode);
          target.onStart();
        }
        containers[i].doLaunch((lockLastElement && i == containers.length - 1), isOnStart);
      }
    }else if(target!=null){
      if(spawnMode == 1 && !isOnStart) {
        kernel = new Kernel();
        kernel.launch(target, lockLastElement);
      }else if(isOnStart){
        System.out.printf("=> launch: %s mode=%d\n",targetName,spawnMode);
        target.onStart();
      }
    }
  }

  public void stop(){
    doStop(false);
    doStop(true);
  }

  private void doStop(boolean isOnStop) {
    if(containers!=null) {
      for(int i=containers.length-1;i>=0;i--){
        containers[i].doStop(isOnStop);
        if(isOnStop && target != null){
          target.onStop();
        }
      }
    }else if(target!=null){
      if(spawnMode == 1 && !isOnStop) {
        if (kernel != null) {
          kernel.stopThread();
          kernel = null;
        }
      }else if(isOnStop){
        target.onStop();
      }
    }
  }
}

