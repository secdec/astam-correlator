package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.*;

public class DefaultRailsRouter implements RailsRouter {

    @Override
    public RailsRoutingEntry identify(String identifier) {
        if (identifier.endsWith(".draw")) {
            return new DrawEntry();
        } else if (identifier.equalsIgnoreCase("get") ||
                identifier.equalsIgnoreCase("put") ||
                identifier.equalsIgnoreCase("post") ||
                identifier.equalsIgnoreCase("delete")) {
            return new DirectHttpEntry();
        } else if (identifier.equalsIgnoreCase("resources")) {
            return new ResourcesEntry();
        } else if (identifier.equalsIgnoreCase("resource")) {
            return new ResourceEntry();
        } else if (identifier.equalsIgnoreCase("match")) {
            return new MatchEntry();
        } else if (identifier.equalsIgnoreCase("namespace")) {
            return new NamespaceEntry();
        } else if (identifier.equalsIgnoreCase("collection")) {
            return new CollectionEntry();
        } else if (identifier.equalsIgnoreCase("concern")) {
            return new ConcernEntry();
        } else if (identifier.equalsIgnoreCase("concerns")) {
            return new ConcernsEntry();
        } else if (identifier.equalsIgnoreCase("member")) {
            return new MemberEntry();
        } else if (identifier.equalsIgnoreCase("root")) {
            return new RootEntry();
        } else if (identifier.equalsIgnoreCase("scope")) {
            return new ScopeEntry();
        } else if (identifier.equalsIgnoreCase("controller")) {
            return new ControllerEntry();
        } else {
            return new UnknownEntry();
        }
    }
}
