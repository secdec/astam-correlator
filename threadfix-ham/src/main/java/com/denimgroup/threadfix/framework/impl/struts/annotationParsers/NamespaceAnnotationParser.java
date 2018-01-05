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

package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.NamespaceAnnotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class NamespaceAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> parsedNamespaces = list();
    List<NamespaceAnnotation> pendingNamespaces = list();

    NamespaceAnnotation currentNamespace = null;

    @Override
    public Collection<Annotation> getAnnotations() {
        return parsedNamespaces;
    }

    @Override
    protected String getAnnotationName() {
        return "Namespace";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentNamespace = new NamespaceAnnotation();
        currentNamespace.setCodeLine(lineNumber);
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingNamespaces.add(currentNamespace);
        parsedNamespaces.add(currentNamespace);
        currentNamespace = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType, int lineNumber) {
        for (NamespaceAnnotation annotation : pendingNamespaces) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        pendingNamespaces.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex, int lineNumber) {
        if (parameterIndex == 0) {
            currentNamespace.setNamespacePath(value);
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value, int lineNumber) {
        if (name.equals("value")) {
            currentNamespace.setNamespacePath(value);
        }
    }
}
