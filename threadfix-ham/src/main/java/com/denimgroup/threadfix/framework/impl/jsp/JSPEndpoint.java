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
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.data.entities.ExplicitEndpointPathNode;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.WildcardEndpointPathNode;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.engine.CodePoint;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.*;

public class JSPEndpoint extends AbstractEndpoint {

    @Nonnull
    private String dynamicPath, staticPath;

    @Nonnull
    private final Map<String, RouteParameter> parameters = map();
    @Nonnull
    private final Map<Integer, List<RouteParameter>> lineToParamMap;

    @Nonnull
    private String method;

    private int startLine = -1, endLine = -1;

    private JSPEndpoint() {
        this.lineToParamMap = map();
    }

    public JSPEndpoint(@Nonnull String staticPath,
                       @Nonnull String dynamicPath,
                       @Nonnull String method,
                       @Nonnull Map<Integer, List<RouteParameter>> lineToParamMap) {

        this.method = method;
        this.staticPath = staticPath;
        this.dynamicPath = dynamicPath;
        this.lineToParamMap = lineToParamMap;

        this.dynamicPath = this.dynamicPath.replaceAll("\\\\", "/");

        List<Integer> sortedLineNumbers = new ArrayList<Integer>(lineToParamMap.keySet());
        Collections.sort(sortedLineNumbers);

        for (Integer lineNo : sortedLineNumbers) {
            List<RouteParameter> paramsAtLine = lineToParamMap.get(lineNo);
            for (RouteParameter param : paramsAtLine) {
                if (!parameters.containsKey(param.getName())) {
                    parameters.put(param.getName(), param);
                }
            }
        }
    }

    @Override
    public int compareRelevance(String checkedPath) {

        int relevance = super.compareRelevance(checkedPath);

        if (relevance > 0) {
            return relevance;
        } else {
            relevance = 0;
        }

        String[] pathParts = CodeParseUtil.trim(checkedPath, "/").split("/");
        String[] endpointParts = CodeParseUtil.trim(dynamicPath, "/").split("/");

        int numMatchedParts = 0;

        for (int i = 0; i < pathParts.length && i < endpointParts.length; i++) {
            String currentPathPart = pathParts[i];
            String currentEndpointPart = endpointParts[i];
            String currentEndpointPartFormat;

            if (i == endpointParts.length - 1) {
                currentEndpointPartFormat = currentEndpointPart.replace("*", ".*");
            } else {
                currentEndpointPartFormat = currentEndpointPart.replace("*", "[^/]*");
            }

            if (currentPathPart.equalsIgnoreCase(currentEndpointPart)) {
                relevance += currentEndpointPart.length();
                ++numMatchedParts;
            } else {

                Matcher partMatcher = Pattern.compile(currentEndpointPartFormat).matcher(currentPathPart);
                if (!partMatcher.find()) {
                    return 0;
                } else {
                    relevance += currentEndpointPart.length();
                    ++numMatchedParts;
                }
            }
        }

        return relevance + numMatchedParts * 100;
    }

    @Override
    public boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness) {
        boolean isGenerallyRelevant = compareRelevance(endpoint) >= 0;
        if (strictness == EndpointRelevanceStrictness.LOOSE) {
            return isGenerallyRelevant;
        } else if (!isGenerallyRelevant) {
            return false;
        }

        String[] thisEndpointParts = StringUtils.split(CodeParseUtil.trim(dynamicPath, "/"), '/');
        String[] endpointParts = StringUtils.split(CodeParseUtil.trim(endpoint, "/"), '/');

        if (thisEndpointParts.length != endpointParts.length) {
            return false;
        }

        for (int i = 0; i < thisEndpointParts.length; i++) {
            String thisPart = thisEndpointParts[i];
            String part = endpointParts[i];

            if (!thisPart.equalsIgnoreCase(part) && !thisPart.contains("*")) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    String getParameterName(@Nonnull Iterable<CodePoint> codePoints) {
        String parameter = null;

        for (CodePoint codePoint : codePoints) {
            List<RouteParameter> possibleParameters = lineToParamMap.get(codePoint.getLineNumber());

            if (possibleParameters != null && possibleParameters.size() == 1) {
                parameter = possibleParameters.get(0).getName();
                break;
            }
        }

        return parameter;
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return dynamicPath;
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {
        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();

        String[] pathParts = StringUtils.split(dynamicPath, '/');
        for (String part : pathParts) {
            if (part.contains("*")) {
                result.add(new WildcardEndpointPathNode(part.replaceAll("\\*", ".*")));
            } else {
                result.add(new ExplicitEndpointPathNode(part));
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        return method;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return true; // JSPs aren't controller-based, so the whole page is the endpoint
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return staticPath;
    }

    @Override
    public int getStartingLineNumber() {
        return startLine;
    }

    @Override
    public int getEndingLineNumber() {
        return endLine;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        for (Map.Entry<Integer, List<RouteParameter>> entry : lineToParamMap.entrySet()) {
            for (RouteParameter lineParam : entry.getValue()) {
                if (lineParam.getName().equalsIgnoreCase(parameter)) {
                    return entry.getKey();
                }
            }
        }
        return 0;
    }

    public void setLines(int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }


    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return list();
    }
}
