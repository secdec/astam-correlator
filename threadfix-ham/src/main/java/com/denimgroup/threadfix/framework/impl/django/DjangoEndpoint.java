// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.setFrom;

/**
 * Created by csotomayor on 4/25/2017.
 */
public class DjangoEndpoint extends AbstractEndpoint {

    private String filePath;
    private String urlPath;

    private Set<String> httpMethods;
    private Map<String, ParameterDataType> parameters;

    public DjangoEndpoint(String filePath, String urlPath,
                          Collection<String> httpMethods, Map<String, ParameterDataType> parameters) {
        this.filePath = filePath;
        this.urlPath = urlPath;
        if (httpMethods != null)
            this.httpMethods = setFrom(httpMethods);
        if (parameters != null)
            this.parameters = parameters;
    }

    @Override
    public int compareRelevance(String endpoint) {
        if (endpoint.equalsIgnoreCase(urlPath)) {
            return 100;
        } else {
            return -1;
        }
    }

    @Nonnull
    @Override
    public Map<String, ParameterDataType> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return 0;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return 0;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return false;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }
}
