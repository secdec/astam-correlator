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
package com.denimgroup.threadfix.framework.impl.spring;

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.util.RegexUtils;
import com.denimgroup.threadfix.framework.util.java.EntityMappings;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.*;

public class SpringControllerEndpoint extends AbstractEndpoint {

    @Nonnull
    private String filePath, urlPath;
    private Pattern urlPathPattern;
    @Nonnull
    private Map<String, RouteParameter> parameters;
    private int startLineNumber = -1, endLineNumber = -1;

    private String method;
    private AuthenticationRequired authenticationRequired = AuthenticationRequired.UNKNOWN;
    private String authorizationString;

    @JsonIgnore
    @Nullable
    private ModelField modelObject;

    @JsonIgnore
    @Nullable
    private SpringDataBinderParser dataBinderParser = null;

    private SpringControllerEndpoint() {

    }

    public SpringControllerEndpoint(@Nonnull String filePath,
                                    @Nonnull String urlPath,
                                    @Nonnull String method,
                                    @Nonnull Map<String, RouteParameter> parameters,
                                    int startLineNumber,
                                    int endLineNumber,
                                    @Nullable ModelField modelObject) {

        this.filePath = filePath;
        this.urlPath = urlPath;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;

        this.urlPath = this.urlPath
            .replaceAll("\\\\", "/")
            .replaceAll("\\.html", "");

        this.modelObject = modelObject;

        this.parameters = parameters;
        this.method = method;

        this.urlPathPattern = Pattern.compile(
            urlPath
                .replaceAll("\\{[^\\}]+\\}", "[^\\/]+")
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\*\\*", ".*")
                .replaceAll("([^\\.])\\*", "$1.*")
        );
    }

    /**
     * TODO change this API, the globalDataBinderParser is confusing
     * Right now this method requires the local DataBinderParser to be set already
     * @param entityMappings entity mappings from the application
     * @param globalDataBinderParser can be null, if a databinderparser is set with the setter it will be used too
     */
    public void expandParameters(@Nonnull EntityMappings entityMappings,
                                 @Nullable SpringDataBinderParser globalDataBinderParser) {
        if (modelObject != null) {
            ModelFieldSet fields = entityMappings.getPossibleParametersForModelType(modelObject);
            parameters.putAll(fields.getPossibleParameters());
        }

        Set<String> allowedParams = null, disallowedParams = null;

        if (dataBinderParser != null) {
            if (dataBinderParser.hasBlacklist) {
                disallowedParams = dataBinderParser.parametersBlackList;
            }
            if (dataBinderParser.hasWhitelist) {
                allowedParams = dataBinderParser.parametersWhiteList;
            }
        }

        if (globalDataBinderParser != null) {
            if (globalDataBinderParser.hasBlacklist && disallowedParams != null) {
                disallowedParams = globalDataBinderParser.parametersBlackList;
            }
            if (globalDataBinderParser.hasWhitelist && allowedParams == null) {
                allowedParams = globalDataBinderParser.parametersWhiteList;
            }
        }

        //  Apply whitelist/blacklist
        //  Automatically allow any embedded endpoint parameters

        if (disallowedParams != null) {
            disallowedParams = new HashSet<String>(disallowedParams);
            for (RouteParameter param : parameters.values()) {
                if (param.getParamType() == RouteParameterType.PARAMETRIC_ENDPOINT && disallowedParams.contains(param.getName())) {
                    disallowedParams.remove(param.getName());
                }
            }
            parameters.keySet().removeAll(disallowedParams);
        }
        if (allowedParams != null) {
            allowedParams = new HashSet<String>(allowedParams);
            for (RouteParameter param : parameters.values()) {
                if (param.getParamType() == RouteParameterType.PARAMETRIC_ENDPOINT && !allowedParams.contains(param.getName())) {
                    allowedParams.add(param.getName());
                }
            }
            parameters.keySet().retainAll(allowedParams);
        }
    }

    @Override
    public int compareRelevance(String endpoint) {
        if (getUrlPath().equalsIgnoreCase(endpoint)) {
            return 100;
        } else if (urlPathPattern.matcher(endpoint).find()) {
            return urlPath.length();
        } else {
            return -1;
        }
    }

    @Override
    public boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness) {
        if (getUrlPath().equalsIgnoreCase(endpoint)) {
            return true;
        } else if (strictness == EndpointRelevanceStrictness.LOOSE) {
            return urlPathPattern.matcher(endpoint).find();
        } else {
            return endpoint.replaceFirst(urlPathPattern.pattern(), "").length() == 0;
        }
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    public void setDataBinderParser(@Nullable SpringDataBinderParser dataBinderParser) {
        this.dataBinderParser = dataBinderParser;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return lineNumber <= endLineNumber && lineNumber >= startLineNumber;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        List<String> finalList = list("Permissions:");
        finalList.addAll(getRequiredPermissions());
        return finalList;
    }

    @Nonnull
    @Override
    public String toString() {
        return "[" + filePath +
                ":" + startLineNumber +
                "-" + endLineNumber +
                " -> " + getHttpMethod() +
                " " + urlPath +
                " " + getParameters() +
                "]";
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        return method;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        //String path = getCleanedUrlPath();
        String path = urlPath;
        if (path != null) {
            return path;
        } else {
            return "";
        }
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {
        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();

        String[] parts = StringUtils.split(getUrlPath(), '/');
        for (String part : parts) {
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
        return startLineNumber;
    }

    @Override
    public int getEndingLineNumber() {
        return endLineNumber;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return startLineNumber;
    }



    public String getAuthorizationString() {
        return authorizationString;
    }

    public void setAuthorizationString(String authorizationString) {
        this.authorizationString = authorizationString;
    }

    private static Pattern AUTHORIZATION_PATTERN = Pattern.compile("hasRole\\('([^']+)'\\)");

    @Override
    @Nonnull
    public List<String> getRequiredPermissions() {
        List<String> permissions = list();
        if (authorizationString != null) {
            permissions.addAll(RegexUtils.getRegexResults(authorizationString, AUTHORIZATION_PATTERN));
        }

        return permissions;
    }

    @Nonnull
    @Override
    public AuthenticationRequired getAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(AuthenticationRequired authenticationRequired) {
        this.authenticationRequired = authenticationRequired;
    }
}
