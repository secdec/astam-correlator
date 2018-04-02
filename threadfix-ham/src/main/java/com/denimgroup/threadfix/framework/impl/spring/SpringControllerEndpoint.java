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
import com.denimgroup.threadfix.data.enums.ParameterDataType;
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
	
	public static final String GENERIC_INT_SEGMENT = "{id}";
	private static final String requestMappingStart = "RequestMethod.";
	
	@Nonnull
    private String rawFilePath, rawUrlPath;
	Pattern rawUrlPathPattern;
	@Nonnull
    private Map<String, RouteParameter> parameters;
	private int startLineNumber = -1, endLineNumber = -1;

	private String method;
	
	@Nullable
    private String cleanedFilePath = null, cleanedUrlPath = null;

    private AuthenticationRequired authenticationRequired = AuthenticationRequired.UNKNOWN;
    private String fileRoot, authorizationString;

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

        this.rawFilePath = filePath;
        this.rawUrlPath = urlPath;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;

        this.rawUrlPath = this.rawUrlPath.replaceAll("\\\\", "/");

        this.modelObject = modelObject;

        this.parameters = parameters;
        this.method = method;

        this.rawUrlPathPattern = Pattern.compile(urlPath.replaceAll("\\{.+\\}", "[^\\/]+"));
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

        if (disallowedParams != null) {
            parameters.keySet().removeAll(disallowedParams);
        }
        if (allowedParams != null) {
            parameters.keySet().retainAll(allowedParams);
        }
    }

    @Nonnull
	private Set<String> getCleanedSet(@Nonnull Collection<String> methods) {
		Set<String> returnSet = set();
		for (String method : methods) {
			if (method.startsWith(requestMappingStart)) {
				returnSet.add(method.substring(requestMappingStart.length()));
			} else {
				returnSet.add(method);
			}
		}
		
		if (returnSet.isEmpty()) {
			returnSet.add("GET");
		}
		
		return returnSet;
	}

    @Override
    public int compareRelevance(String endpoint) {
        if (getUrlPath().equalsIgnoreCase(endpoint)) {
            return 100;
        } else if (rawUrlPathPattern.matcher(endpoint).find()) {
            return rawUrlPath.length();
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
    public String getCleanedFilePath() {
		if (cleanedFilePath == null && fileRoot != null &&
				rawFilePath.contains(fileRoot)) {
			cleanedFilePath = rawFilePath.substring(fileRoot.length());
		}

        if (cleanedFilePath == null) {
            return rawFilePath;
        }
		
		return cleanedFilePath;
	}
	
	public void setFileRoot(String fileRoot) {
		this.fileRoot = fileRoot;
	}

    public void setDataBinderParser(@Nullable SpringDataBinderParser dataBinderParser) {
        this.dataBinderParser = dataBinderParser;
    }
	
	@Nullable
    public static String cleanUrlPathStatic(@Nullable String rawUrlPath) {
		if (rawUrlPath == null) {
			return null;
		} else {
			return rawUrlPath
					.replaceAll("/\\*/", "/" + GENERIC_INT_SEGMENT + "/")
					.replaceAll("\\{[^\\}]+\\}", GENERIC_INT_SEGMENT);
		}
	}
	
	@Override
	public boolean matchesLineNumber(int lineNumber) {
		return lineNumber < endLineNumber && lineNumber > startLineNumber;
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
		return "[" + getCleanedFilePath() +
				":" + startLineNumber +
				"-" + endLineNumber +
				" -> " + getHttpMethod() +
				" " + rawUrlPath +
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
        String path = rawUrlPath;
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
		return getCleanedFilePath();
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

    Pattern pattern = Pattern.compile("hasRole\\('([^']+)'\\)");
    List<String> permissions = null;

    @Override
    @Nonnull
    public List<String> getRequiredPermissions() {

        if (permissions == null) {
            permissions = list();
            if (authorizationString != null) {
                permissions.addAll(RegexUtils.getRegexResults(authorizationString, pattern));
            }
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
