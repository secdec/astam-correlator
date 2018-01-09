package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;

public interface Concernable extends RailsRoutingEntry {
    Collection<String> getConcerns();
    void resetConcerns();
}
