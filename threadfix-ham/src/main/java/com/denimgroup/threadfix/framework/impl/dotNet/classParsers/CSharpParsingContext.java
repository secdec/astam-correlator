////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

public class CSharpParsingContext {
    private CSharpAttributeParser attributeParser;
    private CSharpClassParser classParser;
    private CSharpMethodParser methodParser;
    private CSharpParameterParser parameterParser;
    private CSharpScopeTracker scopeTracker;

    public CSharpParsingContext(
        CSharpAttributeParser attributeParser,
        CSharpClassParser classParser,
        CSharpMethodParser methodParser,
        CSharpParameterParser parameterParser,
        CSharpScopeTracker scopeTracker
    ) {
        this.attributeParser = attributeParser;
        this.classParser = classParser;
        this.methodParser = methodParser;
        this.parameterParser = parameterParser;
        this.scopeTracker = scopeTracker;
    }

    public CSharpAttributeParser getAttributeParser() {
        return attributeParser;
    }

    public CSharpClassParser getClassParser() {
        return classParser;
    }

    public CSharpMethodParser getMethodParser() {
        return methodParser;
    }

    public CSharpParameterParser getParameterParser() {
        return parameterParser;
    }

    public CSharpScopeTracker getScopeTracker() {
        return scopeTracker;
    }

    public void applyToParsers() {
        if (attributeParser != null)
            attributeParser.setParsingContext(this);
        if (classParser != null)
            classParser.setParsingContext(this);
        if (methodParser != null)
            methodParser.setParsingContext(this);
        if (parameterParser != null)
            parameterParser.setParsingContext(this);
    }
}
