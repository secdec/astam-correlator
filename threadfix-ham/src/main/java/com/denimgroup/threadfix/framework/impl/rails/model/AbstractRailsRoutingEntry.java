package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class AbstractRailsRoutingEntry implements RailsRoutingEntry {

    RailsRoutingEntry parentEntry;
    List<RailsRoutingEntry> children = list();
    List<RouteCommonParameter> commonParameters = list();
    List<RouteShorthand> shorthands = list();


    @Override
    public void onBegin(String identifier) {

    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {

    }

    @Override
    public void onInitializerParameter(int index, String value) {

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


    @Override
    public Collection<RouteCommonParameter> getCommonParameters() {
        return commonParameters;
    }

    @Override
    public void addDecorator(RouteCommonParameter decorator) {
        commonParameters.add(decorator);
    }

    @Override
    public Collection<RouteShorthand> getShorthands() {
        return shorthands;
    }

    @Override
    public void addShorthand(RouteShorthand shorthand) {
        shorthands.add(shorthand);
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

    protected String extractController(String controllerDescriptor) {
        if (!controllerDescriptor.contains("#")) {
            return controllerDescriptor;
        } else {
            return controllerDescriptor.split("#")[0];
        }
    }

    protected List<String> buildParentPaths() {
        List<String> paths = list();
        if (parentEntry != null) {
            for (PathHttpMethod httpPath : parentEntry.getSubPaths()) {
                paths.add(httpPath.getPath());
            }
        }
        return paths;
    }

    protected String makeRelativePathToParent(String path) {
        String parentPath = getParent().getPrimaryPath();
        return PathUtil.combine(parentPath, path);
    }

    protected void cloneChildrenInto(RailsRoutingEntry target) {
        for (RailsRoutingEntry entry : children) {
            target.addChildEntry(entry.cloneEntry());
        }
    }

    @Override
    public Collection<RouteCommonParameter> getSupportedDecorators() {
        return null;
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return null;
    }
}
