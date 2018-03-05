////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

/**
 * Created by mac on 6/26/14.
 */
class Action {
    @Nonnull
    String      name;
    @Nonnull
    Set<String> attributes;
    @Nonnull
    Integer     lineNumber = -1;
    @Nonnull
    Integer     endLineNumber = -1;
    @Nonnull
    Map<String, RouteParameter> parameters = map();
    @Nonnull
    Set<RouteParameter> parametersWithTypes;

    List<String> getMethods() {
        List<String> methods = list();

        if (attributes.contains("HttpGet")) {
            methods.add("GET");
        }
        if (attributes.contains("HttpPost")) {
            methods.add("POST");
        }
        if (attributes.contains("HttpPatch")) {
            methods.add("PATCH");
        }
        if (attributes.contains("HttpPut")) {
            methods.add("PUT");
        }
        if (attributes.contains("HttpDelete")) {
            methods.add("DELETE");
        }

        return methods;
    }

    static Action action(@Nonnull String name,
                         @Nonnull Set<String> attributes,
                         @Nonnull Integer lineNumber,
                         @Nonnull Integer endLineNumber,
                         @Nonnull Set<RouteParameter> parametersWithTypes) {
        Action action = new Action();
        action.name = name;
        action.attributes = attributes;
        action.lineNumber = lineNumber;
        action.endLineNumber = endLineNumber;
        action.parametersWithTypes = set();

        Map<String, List<RouteParameter>> duplicateParameterData = map();

        for (RouteParameter param : parametersWithTypes) {
            if (param.getDataTypeSource() != null && param.getDataTypeSource().equals("Include")) {
                for (String s : StringUtils.split(param.getName(), ',')) {
                    s = s.trim();

                    RouteParameter actionParam = new RouteParameter(s);
                    actionParam.setParamType(param.getParamType());

                    List<RouteParameter> duplicateRecord = duplicateParameterData.get(s);
                    if (duplicateRecord == null) {
                        duplicateParameterData.put(s, duplicateRecord = list());
                    }
                    duplicateRecord.add(actionParam);
                }
            } else {
                String paramName = param.getName().trim();
                RouteParameter actionParam = new RouteParameter(paramName);
                actionParam.setDataType(param.getDataTypeSource());
                actionParam.setParamType(param.getParamType());

                List<RouteParameter> duplicateRecord = duplicateParameterData.get(paramName);
                if (duplicateRecord == null) {
                    duplicateParameterData.put(paramName, duplicateRecord = list());
                }
                duplicateRecord.add(actionParam);
            }
        }

        // Cross-reference duplicate parameters and determine which properties are most likely correct
        for (Map.Entry<String, List<RouteParameter>> entry : duplicateParameterData.entrySet()) {
            String paramName = entry.getKey();
            List<RouteParameter> duplicates = entry.getValue();
            if (duplicates.size() == 1) {
                action.parameters.put(paramName, duplicates.get(0));
                action.parametersWithTypes.add(duplicates.get(0));
                continue;
            }

            RouteParameter consolidatedParameter = null;

            for (RouteParameter duplicate : duplicates) {
                // Start with the properties of the first listed parameter
                if (consolidatedParameter == null) {
                    consolidatedParameter = new RouteParameter(paramName);
                    consolidatedParameter.setParamType(duplicate.getParamType());
                    consolidatedParameter.setDataType(duplicate.getDataTypeSource());
                    consolidatedParameter.setAcceptedValues(duplicate.getAcceptedValues());
                } else {

                    boolean needsBetterParamType = consolidatedParameter.getParamType() == RouteParameterType.UNKNOWN;
                    boolean needsAcceptedValues = consolidatedParameter.getAcceptedValues() == null || consolidatedParameter.getAcceptedValues().size() == 0;
                    boolean needsBetterDataType =
                                    consolidatedParameter.getDataType() == null ||
                                    consolidatedParameter.getDataType().getDisplayName().equals(ParameterDataType.STRING.getDisplayName());

                    if (needsBetterParamType) {
                        consolidatedParameter.setParamType(duplicate.getParamType());
                    }

                    if (needsAcceptedValues) {
                        consolidatedParameter.setAcceptedValues(duplicate.getAcceptedValues());
                    }

                    if (needsBetterDataType) {
                        consolidatedParameter.setDataType(duplicate.getDataTypeSource());
                    }
                }
            }

            action.parameters.put(paramName, consolidatedParameter);
            action.parametersWithTypes.add(consolidatedParameter);
        }



        return action;
    }

    @Override
    public String toString() {
        return name + ": " + getMethods() + parameters;
    }

}

