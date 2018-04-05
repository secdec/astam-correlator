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
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.ExplicitEndpointPathNode;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.WildcardEndpointPathNode;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetEndpoint extends AbstractEndpoint {

    @Nonnull String path;
    @Nonnull String filePath;
    @Nonnull Action action;

    Pattern pathPattern;
    String forcedMethod = null;

    private DotNetEndpoint() {

    }

    public DotNetEndpoint(@Nonnull String path, @Nonnull String filePath, @Nonnull Action action) {
        this.path = path;
        this.path = this.path.replaceAll("\\\\", "/");
        if (this.path.endsWith("/") && this.path.length() > 1) {
            this.path = this.path.substring(0, this.path.length() - 1);
        }
        this.filePath = filePath;
        this.action = action;
        this.pathPattern = Pattern.compile(path.replaceAll("\\{.+\\}", "[^\\/]+"));
    }

    @Override
    public int compareRelevance(String endpoint) {
        if (endpoint.equalsIgnoreCase(path)) {
            return 100;
        } else if (pathPattern.matcher(endpoint).find()) {
            return path.length();
        } else {
            return -1;
        }
    }

    @Override
    public boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness) {
        boolean isGenerallyRelevant = compareRelevance(endpoint) >= 0;
        if (strictness == EndpointRelevanceStrictness.LOOSE) {
            return isGenerallyRelevant;
        } else if (!isGenerallyRelevant) {
            return false;
        }

        if (endpoint.equalsIgnoreCase(path)) {
            return true;
        }

        //  At this point the endpoint must at least contain the expected pattern
        //  The endpoint will be well-matched if the whole endpoint matches the whole pattern
        String strippedEndpoint = endpoint.replaceFirst(pathPattern.pattern(), "");
        strippedEndpoint = CodeParseUtil.trim(strippedEndpoint, "/");

        return strippedEndpoint.length() == 0;
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return action.parameters;
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        if (forcedMethod != null) {
            return forcedMethod;
        } else if (action.getMethods().size() > 0) {
            return action.getMethods().get(0);
        } else {
            return "GET";
        }
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return path;
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {

        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();

        String[] pathParts = StringUtils.split(path, '/');
        for (String part : pathParts) {
            if (part.contains("{")) {
                result.add(new WildcardEndpointPathNode(null));
            } else {
                result.add(new ExplicitEndpointPathNode(part));
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return action.lineNumber;
    }

    @Override
    public int getEndingLineNumber() {
        return action.endLineNumber;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return -1;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return lineNumber >= action.lineNumber && lineNumber <= action.endLineNumber;
    }

    public boolean hasMultipleMethods() {
        return action.getMethods().size() > 1;
    }

    public List<DotNetEndpoint> splitByMethods() {
        List<DotNetEndpoint> endpoints = list();

        for (String method : action.getMethods()) {
            DotNetEndpoint dedicatedMethodEndpoint = new DotNetEndpoint(this.path, this.filePath, this.action);
            dedicatedMethodEndpoint.forcedMethod = method;
            endpoints.add(dedicatedMethodEndpoint);
        }

        return endpoints;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        List<String> lintLines = list();

        if (!action.attributes.contains("HttpPost") && !action.attributes.contains("HttpGet")) {
            lintLines.add("No HTTP method limiting annotation ([HttpGet], [HttpPost]) found.");
        }

        if (!action.attributes.contains("ValidateAntiForgeryToken")) {
            lintLines.add("[ValidateAntiForgeryToken] missing.");
        }

        return lintLines;
    }
}
