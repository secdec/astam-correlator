package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import javax.annotation.Nonnull;
import java.util.Collection;

//  See: http://www.rubydoc.info/github/plataformatec/devise/master/ActionDispatch/Routing/Mapper#devise_scope-instance_method

//  Doesn't actually do anything - this is a detail regarding which model type to use, which isn't relevant for
//  us at the moment.
public class DeviseScopeEntry extends AbstractRailsRoutingEntry {

    @Override
    public String getModule() {
        return getParentModule();
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

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        return null;
    }
}
