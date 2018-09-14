package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameterMap extends HashMap<Integer, List<RouteParameter>> {

    public void put(Integer lineNumber, RouteParameter parameter) {
        if (!containsKey(lineNumber)) {
            put(lineNumber, new ArrayList<RouteParameter>());
        }

        get(lineNumber).add(parameter);
    }

    public List<RouteParameter> findParametersInLines(int startLine, int endLine) {
        List<RouteParameter> parameters = list();
        for (Map.Entry<Integer, List<RouteParameter>> entry : entrySet()) {
            int lineNumber = entry.getKey();
            if (lineNumber < startLine || lineNumber > endLine) {
                parameters.addAll(entry.getValue());
            }
        }
        return parameters;
    }
}
