package com.denimgroup.threadfix.framework.impl.struts.actionEndpointEnumerators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActionEndpointEnumerator {
    public Collection<String> getPossibleEndpoints(String baseEndpoint, String[] actionExtensions) {
        if (baseEndpoint.endsWith("/")) {
            baseEndpoint = baseEndpoint.substring(0, baseEndpoint.length() - 1);
        }
        List<String> result = new ArrayList<String>();
        for (String strutsExtension : actionExtensions) {
            if (strutsExtension.length() > 0 && !strutsExtension.startsWith(".")) {
                strutsExtension = "." + strutsExtension;
            }
            result.add(baseEndpoint + strutsExtension);
        }

        return result;
    }
}
