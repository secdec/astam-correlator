package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DotNetMappingsCoreGenerator implements DotNetMappingsGenerator {

    private List<CSharpClass> classes;
    private Map<String, RouteParameterMap> routeParameters;

    public DotNetMappingsCoreGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;
    }

    @Override
    public List<DotNetControllerMappings> generate() {
        return list();
    }
}
