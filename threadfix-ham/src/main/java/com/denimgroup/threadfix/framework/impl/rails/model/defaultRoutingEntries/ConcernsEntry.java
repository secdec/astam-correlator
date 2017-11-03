package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsEntryShorthand;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ConcernsEntry extends AbstractRailsRoutingEntry implements Concernable {

    Collection<String> concernIds = list();

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
    public void setControllerName(String controllerName) {

    }

    @Override
    public void setActionMethodName(String actionMethodName) {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("concerns [");
        if (concernIds != null) {
            for (String concern : concernIds) {
                result.append(":");
                result.append(concern);
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return list((RouteShorthand)new ConcernsEntryShorthand());
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        ConcernsEntry clone = new ConcernsEntry();
        clone.concernIds.addAll(concernIds);
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<String> getConcerns() {
        return concernIds;
    }

    @Override
    public void resetConcerns() {

    }
}
