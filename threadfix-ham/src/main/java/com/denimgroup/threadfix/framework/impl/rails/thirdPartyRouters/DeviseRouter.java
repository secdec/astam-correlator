package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseForEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseScopeEntry;

public class DeviseRouter implements RailsRouter {
    @Override
    public RailsRoutingEntry identify(String identifier) {
        if (identifier.equalsIgnoreCase("devise_for")) {
            return new DeviseForEntry();
        } else if (identifier.equalsIgnoreCase("devise_scope")) {
            return new DeviseScopeEntry();
        } else {
            return null;
        }
    }
}
