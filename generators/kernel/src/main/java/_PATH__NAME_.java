<%
import sys
sys.path.insert(0, a.parserPath)

import parsing_java
p = reload(parsing_java)
p.parsingGernet(a)

%>
package ${a.package};

/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile,module=configModule))
]]]*/
/*[[[end]]]*/

  private void onCreate(){

  }

  @Override
  public void onDestroy(){

  }

  private void onKernels(){

  }

  @Override
  public void onStart(){

  }

  @Override
  public void run(){
    ${p.runBlocks(a)}
  }

  @Override
  public void onStop(){

  }

}

