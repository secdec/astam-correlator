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
package com.denimgroup.threadfix.framework.impl.rails;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sgerick on 5/5/2015.
 */
public class RailsEndpoint extends AbstractEndpoint {

    private static String regexCaptureGroupPattern = "([^\\/]+)";

    private String filePath;
    private String urlPath;
    private Pattern urlPattern;

    private String httpMethod;
    private Map<String, RouteParameter> parameters;

    private RailsEndpoint() {

    }

    public RailsEndpoint(String filePath, String urlPath,
                          String httpMethod, Map<String, RouteParameter> parameters) {
        this.filePath = filePath;
        this.urlPath = urlPath;
        if (httpMethod != null)
            this.httpMethod = httpMethod;
        if (parameters != null)
            this.parameters = parameters;

        String urlFormat = urlPath;
        urlFormat = urlFormat
                .replaceAll("\\{.+\\}", regexCaptureGroupPattern)
                .replaceAll("\\:([\\w\\-_]+)", regexCaptureGroupPattern)
                .replaceAll("\\*([\\w\\-_]+)", regexCaptureGroupPattern)
                .replaceAll("\\(\\/\\:([\\w\\-_]+)\\)", "\\/([^\\/]+)");
        urlFormat = "^" + urlFormat + "$";
        urlPattern = Pattern.compile(urlFormat);
    }

    @Override
    public int compareRelevance(String endpoint) {
        if (urlPath.equalsIgnoreCase(endpoint)) {
            return 100;
        } else {
            Matcher matcher = urlPattern.matcher(endpoint);
            if (matcher.find()) {
                return this.urlPath.length();
            } else {
                return -1;
            }
        }
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        return httpMethod;
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

        String simplifiedPath = urlPath.replaceAll(regexCaptureGroupPattern, "*");

        String[] pathParts = StringUtils.split(simplifiedPath, '/');
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
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return -1;
    }

    @Override
    public int getEndingLineNumber() {
        return -1;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return -1;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return true;
    }
}
