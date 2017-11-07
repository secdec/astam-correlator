package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class AbstractRailsRoutingEntry implements RailsRoutingEntry {

    RailsRoutingEntry parentEntry;
    List<RailsRoutingEntry> children = list();


    @Override
    public void onBegin(String identifier) {

    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {

    }

    @Override
    public void onInitializerParameter(String name, String value, RouteParameterValueType parameterType) {

    }

    @Override
    public void onToken(int type, int lineNumber, String stringValue) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void addChildEntry(RailsRoutingEntry child) {
        if (!children.contains(child)) {
            children.add(child);
        }
        child.setParent(this);
    }

    @Override
    public void removeChildEntry(RailsRoutingEntry child) {
        if (children.contains(child)) {
            children.remove(child);
        }
        child.setParent(null);
    }

    @Override
    public Collection<RailsRoutingEntry> getChildren() {
        return children;
    }

    @Override
    public void setParent(RailsRoutingEntry parent) {
        if (this.parentEntry == parent) {
            return;
        }

        if (this.parentEntry != null && this.parentEntry.getChildren().contains(this)) {
            this.parentEntry.removeChildEntry(this);
        }
        parentEntry = parent;
    }

    @Override
    public RailsRoutingEntry getParent() {
        return parentEntry;
    }

    protected String stripColons(String symbol) {
        if (symbol.startsWith(":")) {
            symbol = symbol.substring(1);
        }
        if (symbol.endsWith(":")) {
            symbol = symbol.substring(0, symbol.length() - 1);
        }
        return symbol;
    }

    /**
     * @param controllerDescriptor A string of the format 'controller#action' or 'controller'
     */
    protected String extractController(String controllerDescriptor) {
        if (!controllerDescriptor.contains("#")) {
            return controllerDescriptor;
        } else {
            return controllerDescriptor.split("#")[0];
        }
    }

    /**
     * @param actionDescriptor A string of the format 'controller#action' or 'action'
     */
    protected String extractAction(String actionDescriptor) {
        if (!actionDescriptor.contains("#")) {
            return actionDescriptor;
        } else {
            return actionDescriptor.split("#")[1];
        }
    }

    protected String makeRelativePathToParent(String path) {
        String parentPath;
        if (getParent() != null) {
            parentPath = getParent().getPrimaryPath();
        } else {
            parentPath = "/";
        }
        return PathUtil.combine(parentPath, path);
    }

    protected void cloneChildrenInto(RailsRoutingEntry target) {
        for (RailsRoutingEntry entry : children) {
            target.addChildEntry(entry.cloneEntry());
        }
    }

    protected final String getParentControllerIfNull(String controllerName) {
        if (controllerName != null) {
            return controllerName;
        } else {
            return getParentController();
        }
    }

    protected final String getParentController() {
        String controllerName = null;
        RailsRoutingEntry currentEntry = getParent();
        while (currentEntry != null) {
            controllerName = currentEntry.getControllerName();
            if (controllerName != null)
                break;
            currentEntry = currentEntry.getParent();
        }
        return controllerName;
    }

    protected final String getParentModuleIfNull(String moduleName) {
        if (moduleName != null) {
            return moduleName;
        } else {
            return getParentModule();
        }
    }

    protected final String getParentModule() {
        String moduleName = null;
        RailsRoutingEntry currentEntry = getParent();
        while (currentEntry != null) {
            moduleName = currentEntry.getModule();
            if (moduleName != null)
                break;
            currentEntry = currentEntry.getParent();
        }
        return moduleName;
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return null;
    }
}
