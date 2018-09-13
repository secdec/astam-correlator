package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

public class DotNetParsingContext {
    private DotNetAttributeParser attributeParser;
    private DotNetClassParser classParser;
    private DotNetMethodParser methodParser;
    private DotNetParameterParser parameterParser;
    private DotNetScopeTracker scopeTracker;

    public DotNetParsingContext(
        DotNetAttributeParser attributeParser,
        DotNetClassParser classParser,
        DotNetMethodParser methodParser,
        DotNetParameterParser parameterParser,
        DotNetScopeTracker scopeTracker
    ) {
        this.attributeParser = attributeParser;
        this.classParser = classParser;
        this.methodParser = methodParser;
        this.parameterParser = parameterParser;
        this.scopeTracker = scopeTracker;
    }

    public DotNetAttributeParser getAttributeParser() {
        return attributeParser;
    }

    public DotNetClassParser getClassParser() {
        return classParser;
    }

    public DotNetMethodParser getMethodParser() {
        return methodParser;
    }

    public DotNetParameterParser getParameterParser() {
        return parameterParser;
    }

    public DotNetScopeTracker getScopeTracker() {
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
