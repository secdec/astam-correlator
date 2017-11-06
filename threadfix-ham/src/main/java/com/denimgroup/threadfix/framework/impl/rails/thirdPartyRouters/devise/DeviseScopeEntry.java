package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DeviseScopeEntry extends AbstractRailsRoutingEntry {

    @Override
    public void onToken(int type, int lineNumber, String stringValue) {

    }

    @Override
    public String getPrimaryPath() {
        return null;
    }

    @Override
    public Collection<PathHttpMethod> getSubPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return null;
    }

    @Override
    public String getActionMethodName() {
        return null;
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        return null;
    }
}
