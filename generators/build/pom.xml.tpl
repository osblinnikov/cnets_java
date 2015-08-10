<%import parsing_java
import gernetHelpers
h = reload(gernetHelpers)
p = reload(parsing_java)
p.parsingGernet(a)
dependencies = h.getDependenciesDict(a.read_data)%>

        %for v in dependencies:
        <dependency>
            <groupId>${p.groupId(v["name"])}</groupId>
            <artifactId>${p.artifactId(v["name"])}</artifactId>
            <version>LATEST</version>
            <scope>compile</scope>
            %if v.has_key("type"):
            <type>${v["type"]}</type>
            %endif
        </dependency>
        %endfor