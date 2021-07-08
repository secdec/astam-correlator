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

package com.denimgroup.threadfix.data.interfaces;

import com.denimgroup.threadfix.data.entities.AuthenticationRequired;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface Endpoint extends Comparable<Endpoint> {

    @Nonnull
    Map<String, RouteParameter> getParameters();

    @Nonnull
	String getHttpMethod();

    @Nonnull
	String getUrlPath();

    @Nonnull
    List<EndpointPathNode> getUrlPathNodes();

    @Nonnull
	String getFilePath();

    int compareRelevance(String endpoint);
    boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness);

    @JsonIgnore
    @Nonnull
    List<Endpoint> getVariants();

    @JsonIgnore
    Endpoint getParentVariant();

    @JsonIgnore
    boolean isVariantOf(Endpoint endpoint);

    @JsonIgnore
    boolean isPrimaryVariant();

    enum PrintFormat {
        DYNAMIC, STATIC, LINT, SIMPLE_JSON, FULL_JSON
    }

    @Nonnull
	String getCSVLine(PrintFormat... formats);

	int getStartingLineNumber();
	int getEndingLineNumber();

	int getLineNumberForParameter(String parameter);

	boolean matchesLineNumber(int lineNumber);

    @Nonnull
    public List<String> getRequiredPermissions();

    @Nonnull
    public AuthenticationRequired getAuthenticationRequired();

    public static class Info {
        Map<String, RouteParameter>  parameters;
        String httpMethod;

        String urlPath, filePath, csvLine;

        int startingLineNumber;

        public static Info fromEndpoint(Endpoint endpoint, boolean includeSourceFile) {
            Info info = new Info();
            info.parameters = endpoint.getParameters();
            info.httpMethod = endpoint.getHttpMethod();
            info.urlPath = endpoint.getUrlPath();
            info.csvLine = endpoint.getCSVLine();

            if (includeSourceFile) {
                info.filePath = endpoint.getFilePath();
                info.startingLineNumber = endpoint.getStartingLineNumber();
            }

            return info;
        }

        public Map<String, RouteParameter> getParameters() {
            return parameters;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public String getUrlPath() {
            return urlPath;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getCsvLine() {
            return csvLine;
        }

        public int getStartingLineNumber() {
            return startingLineNumber;
        }
    }
	
}
