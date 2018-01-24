package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.interfaces.Endpoint;

import java.util.List;
import java.util.Map;

public class HyperlinkParameterMerger {

    // Assigns the 'isOptional' property for the given set of 'RouteParameter' objects.
    private void detectOptionalParameters(Map<String, Map<String, List<RouteParameter>>> parameters) {
        // Parameters with fewer entries than the most common entry are assumed to be optional.

        // ie, 'name', 'id', 'password' are all parameters. 'name' occurs twice, 'id' occurs once, and 'password' occurs once.

        // Since the most common entry 'name' has 2 occurrences, and 'id' and 'password' have less than 2 occurrences,
        //    'id' and 'password' are considered optional.

        // This comparison occurs between all QUERY_STRING parameters across all request types. Other parameter types
        //    are only compared within the same request method.

        // NOTE: This will not work for ie forms to the same endpoint whose format depends on the query string.
    }



    private void mergeParsedImplicitParameters(List<Endpoint> endpoints, Map<String, Map<String, List<RouteParameter>>> implicitParameters) {

    }

}
