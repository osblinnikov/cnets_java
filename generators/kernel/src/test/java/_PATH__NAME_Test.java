<%
import sys
sys.path.insert(0, a.parserPath)

import parsing_java
p = reload(parsing_java)
p.parsingGernet(a)

%>
package ${a.package};
import org.junit.Test;
/*[[[cog
import cogging as c
c.tpl(cog,templateFile,c.a(prefix=configFile,module=configModule))
]]]*/
/*[[[end]]]*/
public class ${a.className}Test {
  @Test
  public void test${a.className}(){
    ${p.testRunnables(a)}
  }
}

