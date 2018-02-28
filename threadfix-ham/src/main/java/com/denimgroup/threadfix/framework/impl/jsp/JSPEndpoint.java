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

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.engine.CodePoint;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.*;

public class JSPEndpoint extends AbstractEndpoint {

    @Nonnull
	private String dynamicPath, staticPath;

    @Nonnull
	private final Map<String, RouteParameter> parameters = map();
    @Nonnull
	private String method;

    private int startLine, endLine;

//	@Nonnull
//    private final Map<String, Integer> paramToLineMap;

//	@Nonnull
//    private final Map<Integer, List<String>> parameterMap;

	private JSPEndpoint() {

	}
	
	public JSPEndpoint(@Nonnull String staticPath,
                       @Nonnull String dynamicPath,
                       @Nonnull String method,
			           @Nonnull Map<String, RouteParameter> parameterMap) {
		this.method = method;
		this.staticPath = staticPath;
		this.dynamicPath = dynamicPath;
		this.parameters.putAll(parameterMap);

//		for (List<String> lineParams : parameterMap.values()) {
//			for (String param : lineParams) {
//				parameters.put(param, RouteParameter.fromDataType(ParameterDataType.STRING));
//			}
//		}
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
		boolean isWildCard = dynamicPath.contains("*");

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

//	@Nonnull
//    private Map<String, Integer> getParamToLineMap(
//			Map<Integer, List<String>> parameterMap) {
//		Map<String, Integer> paramMap = map();
//
//		for (String parameter : parameters.keySet()) {
//			paramMap.put(parameter, getFirstLineNumber(parameter, parameterMap));
//		}
//
//		return paramMap;
//	}
	
//	private Integer getFirstLineNumber(@Nonnull String parameterName,
//			@Nonnull Map<Integer, List<String>> parameterMap) {
//		Integer returnValue = Integer.MAX_VALUE;
//
//        for (Map.Entry<Integer, List<String>> entry : parameterMap.entrySet()) {
//            if (entry.getKey() < returnValue &&
//                    entry.getValue() != null &&
//                    entry.getValue().contains(parameterName)) {
//                returnValue = entry.getKey();
//            }
//        }
//
//		if (returnValue == Integer.MAX_VALUE) {
//			returnValue = 1; // This way even if no parameter is found a marker can be created for the file
//		}
//
//		return returnValue;
//	}
	
	// TODO improve
	// TODO - Re-enable
    @Nullable
    String getParameterName(@Nonnull Iterable<CodePoint> codePoints) {
		return null;
//		String parameter = null;
//
//		for (CodePoint codePoint : codePoints) {
//			List<String> possibleParameters = parameterMap.get(codePoint.getLineNumber());
//
//			if (possibleParameters != null && possibleParameters.size() == 1) {
//				parameter = possibleParameters.get(0);
//				break;
//			}
//		}
//
//		return parameter;
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

	// TODO - Re-enable
	@Override
	public int getLineNumberForParameter(String parameter) {
		return -1;
//        Integer value = paramToLineMap.get(parameter);
//        if (value == null) {
//            return 0;
//        } else {
//		    return value;
//        }
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
