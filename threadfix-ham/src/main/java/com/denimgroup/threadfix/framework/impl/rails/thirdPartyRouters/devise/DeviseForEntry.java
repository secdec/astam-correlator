package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DeviseForEntry extends AbstractRailsRoutingEntry {

    String baseEndpoint = null;
    String moduleName = null;

    List<PathHttpMethod> supportedPaths = list(
            //  Default
            new PathHttpMethod("/sign_in", "GET", "new", "devise/sessions"),
            new PathHttpMethod("/sign_in", "POST", "create", "devise/sessions"),
            new PathHttpMethod("/sign_out", "DELETE", "destroy", "devise/sessions"),

            //  There are extra routes if :recoverable or :confirmable are configured, each of
            //  which points to separate controllers

            new PathHttpMethod("/password/new", "GET", "new", "devise/passwords"),
            new PathHttpMethod("/password/edit", "GET", "edit", "devise/passwords"),
            new PathHttpMethod("/password", "PUT", "update", "devise/passwords"),
            new PathHttpMethod("/password", "POST", "create", "devise/passwords"),

            new PathHttpMethod("/confirmation/new", "GET", "new", "devise/confirmations"),
            new PathHttpMethod("/confirmation", "GET", "show", "devise/confirmations"),
            new PathHttpMethod("/confirmation", "POST", "create", "devise/confirmations")
    );

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, value, parameterType);
    }

    @Override
    public String getModule() {
        return getParentModuleIfNull(moduleName);
    }

    @Override
    public String getPrimaryPath() {
        return "/";
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
