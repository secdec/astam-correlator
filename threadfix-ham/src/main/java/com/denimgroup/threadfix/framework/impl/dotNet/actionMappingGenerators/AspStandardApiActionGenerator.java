package com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerMappings;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetParameterUtil;
import com.denimgroup.threadfix.framework.impl.dotNet.RouteParameterMap;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

public class AspStandardApiActionGenerator implements AspActionGenerator {

    List<CSharpClass> classes;
    Map<String, RouteParameterMap> routeParameters;
    ConventionBasedActionGenerator conventionBasedActionGenerator = new ConventionBasedActionGenerator();

    public AspStandardApiActionGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;
    }

    @Override
    public List<DotNetControllerMappings> generate() {

        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isApiControllerClass(csClass)) {
                continue;
            }

            RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
            if (fileParameters == null) {
                fileParameters = new RouteParameterMap();
                routeParameters.put(csClass.getFilePath(), fileParameters);
            }

            DotNetControllerMappings currentMappings = conventionBasedActionGenerator.generateForClass(csClass, fileParameters);

            CSharpAttribute areaAttribute = csClass.getAttribute("RouteArea");
            if (areaAttribute != null && areaAttribute.getParameterValue(0) != null) {
                currentMappings.setAreaName(areaAttribute.getParameterValue(0).getValue());
            }

            findAndAddExplicitMethods(csClass, fileParameters, currentMappings);

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private boolean isApiControllerClass(CSharpClass csClass) {
        return csClass.getName().endsWith("Controller") && csClass.getBaseTypes().contains("ApiController");
    }

    private void findAndAddExplicitMethods(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings) {

    }
}
