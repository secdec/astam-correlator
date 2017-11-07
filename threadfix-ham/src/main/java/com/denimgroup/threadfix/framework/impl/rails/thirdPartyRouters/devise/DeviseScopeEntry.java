package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DeviseScopeEntry extends AbstractRailsRoutingEntry {

    String endpoint = null;
    String moduleName = null;

    @Override
    public String getModule() {
        return getParentModuleIfNull(moduleName);
    }

    @Override
    public String getPrimaryPath() {
        return null;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentController();
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        return null;
    }
}
