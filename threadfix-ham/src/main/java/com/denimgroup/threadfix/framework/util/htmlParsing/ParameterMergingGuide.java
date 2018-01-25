package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

//  We don't have access to modify the data in an Endpoint; instead, provide
//  a guide as to which parameters to add
public class ParameterMergingGuide {

    Map<String, Map<String, List<RouteParameter>>> addedParameters = map();
    Map<String, Map<String, List<RouteParameter>>> removedParameters = map();

}

