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
