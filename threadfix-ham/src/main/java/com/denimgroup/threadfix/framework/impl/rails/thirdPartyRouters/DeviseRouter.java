package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseForEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseScopeEntry;

//  TODO - Check 'config/initializers/devise.rb' to determine loaded support modules
public class DeviseRouter implements RailsRouter {
    @Override
    public RailsRoutingEntry identify(String identifier) {
        if (identifier.equalsIgnoreCase("devise_for")) {
            return new DeviseForEntry();
        } else if (identifier.equalsIgnoreCase("devise_scope") || identifier.equalsIgnoreCase("as")) {
            //  'as' is an alias for 'devise_scope':
            //      http://rubydoc.info/github/plataformatec/devise/master/ActionDispatch/Routing/Mapper#devise_scope-instance_method
            return new DeviseScopeEntry();
        } else {
            return null;
        }
    }
}
