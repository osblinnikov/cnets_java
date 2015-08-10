<%import parsing_java
p = reload(parsing_java)
p.parsingGernet(a)%>

${p.importBlocks(a)}

public class ${a.className} implements RunnableStoppable{
  ${p.getProps(a)}
  ${p.declareBlocks(a)}

  public ${a.className}(${p.getArgs(a)}){
    ${p.getInit(a)}
    ${p.initializeBuffers(a)}
    onKernels();
    ${p.initializeKernels(a)}
    ${p.getRunnables(a)}
    onCreate();
  }

  Reader[] getReaders(){
    %if p.hasRSelector(a):
    return new Reader[]{rSelect};
    %elif p.hasReceive(a):
    return _arrReaders_;
    %else:
    return new Reader[0];
    %endif
  }

  public runnablesContainer getRunnables(){
    return _runnables;
  }