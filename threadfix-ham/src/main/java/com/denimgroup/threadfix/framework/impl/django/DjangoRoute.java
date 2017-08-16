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

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 6/13/2017.
 */
public class DjangoRoute {
    private String url;
    private String viewPath;
    private List<String> httpMethods = list();
    private List<String> parameters = list();

    public DjangoRoute(String url, String viewPath) {
        this.url = url;
        this.viewPath = viewPath;
    }

    public String getUrl() {
        return url;
    }

    public String getViewPath() {
        return viewPath;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void addHttpMethod(String httpMethod) {
        httpMethods.add(httpMethod);
    }

    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
}
