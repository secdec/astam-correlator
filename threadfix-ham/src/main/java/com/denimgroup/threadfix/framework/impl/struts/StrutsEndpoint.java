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
package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.data.entities.ExplicitEndpointPathNode;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.WildcardEndpointPathNode;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StrutsEndpoint extends AbstractEndpoint {

    private String filePath;
    private String urlPath;
    private Pattern pathRegex = null;
    private String displayFilePath = null;
    private int startLine = -1, endLine = -1;

    private String method;
    private Map<String, RouteParameter> parameters;

    private StrutsEndpoint() {

    }

    public StrutsEndpoint(String filePath, String urlPath,
                          String method, Map<String, RouteParameter> parameters) {
        this.filePath = filePath;
        this.urlPath = urlPath;
        this.method = method;
        this.parameters = parameters;

        this.urlPath = this.urlPath.replaceAll("\\\\", "/");

        String regexString = "^" + urlPath
                .replaceAll("\\{.+\\}", "([^\\/]+)")
                .replaceAll("/", "\\\\/");

        if (!regexString.endsWith("*")) {
            regexString += "$";
        }
        pathRegex = Pattern.compile(regexString);
    }

    @Nonnull
    @Override
    public int compareRelevance(String endpoint) {

        if (urlPath.equalsIgnoreCase(endpoint)) {
            return 100;
        } else if (pathRegex.matcher(endpoint).find()) {
            return pathRegex.toString().length();
        } else {
            return -1;
        }
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        return method;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {
        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();
        String[] pathParts = StringUtils.split(urlPath, '/');

        for (String part : pathParts) {
            if (part.contains("{") && part.contains("}")) {
                result.add(new WildcardEndpointPathNode(null));
            } else if (part.contains("*")) {
                result.add(new WildcardEndpointPathNode(part.replaceAll("\\*", ".*")));
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
        return startLine;
    }

    @Override
    public int getEndingLineNumber() {
        return endLine;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return 0;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return lineNumber >= startLine && lineNumber <= endLine;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }

    public void setDisplayFilePath(String displayFilePath) {
        this.displayFilePath = displayFilePath;
    }

    public String getDisplayFilePath() {
        return displayFilePath;
    }

    public void setLineNumbers(int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }
}
