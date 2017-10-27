package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  See: https://struts.apache.org/docs/restfulactionmapper.html

public class Restful2ActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String parentNamespace) {

        List<StrutsEndpoint> endpoints = list();

        for (StrutsPackage strutsPackage : project.getPackages()) {
            String packageNamespace = strutsPackage.getNamespace();

            //  Skip packages that aren't associated with this target namespace
            if (!packageNamespace.startsWith(parentNamespace)) {
                continue;
            }

            Collection<StrutsAction> actions = strutsPackage.getActions();
            for (StrutsAction action : actions) {

            }
        }

        return endpoints;

    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        return list();
    }
}
