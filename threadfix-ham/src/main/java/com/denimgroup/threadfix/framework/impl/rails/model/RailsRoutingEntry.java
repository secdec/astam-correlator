package com.denimgroup.threadfix.framework.impl.rails.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface RailsRoutingEntry {

    void addChildEntry(RailsRoutingEntry child);
    void removeChildEntry(RailsRoutingEntry child);
    List<RailsRoutingEntry> getChildren();
    void setParent(RailsRoutingEntry parent);
    RailsRoutingEntry getParent();
    void setLineNumber(int codeLine);
    int getLineNumber();

    void onToken(int type, int lineNumber, String stringValue);
    void onParameter(String name, String value, RouteParameterValueType parameterType);
    void onInitializerParameter(String name, String value, RouteParameterValueType parameterType);

    void onBegin(String identifier);
    void onEnd();

    /**
     * @return The main path for this entry that will host its endpoints and be the base endpoint for its children.
     */
    String getPrimaryPath();


    /**
     * @return A set of complete endpoints generated directly by this routing entry. The PathHttpMethod
     * should have a URL, HTTP query method, action name, and controller name attached.
     */
    Collection<PathHttpMethod> getPaths();


    /**
     * @return The name of the controller directly assigned to this entry or the name of the closest available controller
     * in this entry's parents.
     */
    String getControllerName();


    /**
     * @return The name of the Ruby module directly assigned to this entry or the name of the closest available module
     * name in this entry's parents.
     */
    String getModule();

    /**
     * @return A set of RouteShorthand implementations that can be used when parsing this route entry.
     */
    Collection<RouteShorthand> getSupportedShorthands();

    /**
     * @return Generates a deep clone of the current entry, duplicating child entries while the clone shares the original parent.
     */
    @Nonnull
    RailsRoutingEntry cloneEntry();
}
