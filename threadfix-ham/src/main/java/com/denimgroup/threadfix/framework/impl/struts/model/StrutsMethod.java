////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.struts.model;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsMethod {

    String methodName;
    List<String> parameters = list();
    List<Annotation> annotations = list();



    public String getName() {
        return methodName;
    }

    public String getUniqueName() {
        StringBuilder uniqueName = new StringBuilder();
        uniqueName.append(methodName);

        for (String param : parameters) {
            uniqueName.append('_');
            uniqueName.append(param);
        }

        return uniqueName.toString();
    }

    public Collection<String> getParameters() {
        return parameters;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }



    public void setName(String name) {
        methodName = name;
    }

    public void addParameter(String name) {
        parameters.add(name);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (annotations.size() > 0) {
            sb.append("@(");
            for (int i = 0; i < annotations.size(); i++) {
                Annotation annotation = annotations.get(i);
                sb.append(annotation.toString());
                if (i < annotations.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");
        }

        sb.append(methodName);
        sb.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
