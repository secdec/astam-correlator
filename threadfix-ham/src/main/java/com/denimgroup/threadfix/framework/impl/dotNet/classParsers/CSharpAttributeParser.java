package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

public class CSharpAttributeParser extends AbstractCSharpParser<CSharpAttribute> implements EventBasedTokenizer {

    private CSharpScopeTracker scopeTracker;

    //  The shared parameter parser for C# parsing
    private CSharpParameterParser parameterParser;

    //  Internally instantiated parameter parsing for parameters of attributes that are attached to parameters, ie
    // IActionResult Post([Bind(Include = "Name, Address")] data)
    private CSharpParsingContext innerParsingContext;
    //  (This is a bit of a shim since arbitrarily embedded parameters/attributes are still not supported)

    @Override
    public void setParsingContext(CSharpParsingContext context) {
        this.parameterParser = context.getParameterParser();
        this.scopeTracker = context.getScopeTracker();
    }

    @Override
    public void reset() {
        super.reset();
        currentAttributeState = AttributeState.SEARCH;
    }

    @Override
    public void resetAll() {
        super.resetAll();
        parameterParser.reset();
    }

    @Override
    public void disableAll() {
        super.disableAll();
        parameterParser.disable();
    }

    public void enableAll() {
        super.enableAll();
        parameterParser.enable();
    }



    private enum AttributeState {
        SEARCH, IN_ATTRIBUTE
    }

    private AttributeState currentAttributeState = AttributeState.SEARCH;
    private int attributeEntryParenLevel = -1;


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (isDisabled() || scopeTracker.isInComment()) {
            return;
        }

        if (innerParsingContext != null) {
            innerParsingContext.getScopeTracker().processToken(type, lineNumber, stringValue);
            innerParsingContext.getParameterParser().processToken(type, lineNumber, stringValue);
            innerParsingContext.getAttributeParser().processToken(type, lineNumber, stringValue);
        }

        switch (currentAttributeState) {
            case SEARCH:
                if (scopeTracker.getNumOpenBracket() == 1 && !parameterParser.isBuildingParameterType()) {
                    currentAttributeState = AttributeState.IN_ATTRIBUTE;
                    setPendingItem(new CSharpAttribute());
                    parameterParser.clearItems();
                    attributeEntryParenLevel = scopeTracker.getNumOpenParen();
                    if (parameterParser.isBuildingItem()) {
                        CSharpParameterParser innerParameterParser = new CSharpParameterParser();
                        CSharpAttributeParser temporaryAttributeParser = new CSharpAttributeParser();
                        CSharpScopeTracker temporaryScopeTracker = new CSharpScopeTracker();
                        innerParsingContext = new CSharpParsingContext(temporaryAttributeParser, null, null, innerParameterParser, temporaryScopeTracker);
                        innerParameterParser.setParsingContext(innerParsingContext);
                        temporaryAttributeParser.setParsingContext(innerParsingContext);
                    }
                }
                break;

            case IN_ATTRIBUTE:
                CSharpAttribute pendingAttribute = getPendingItem();
                if (stringValue != null && pendingAttribute.getName() == null) {
                    pendingAttribute.setName(stringValue);
                }

                CSharpParameterParser currentParameterParser = null;
                if (innerParsingContext != null) {
                    currentParameterParser = innerParsingContext.getParameterParser();
                } else {
                    currentParameterParser = parameterParser;
                }

                while (currentParameterParser.hasItem()) {
                    pendingAttribute.addParameter(currentParameterParser.pullCurrentItem());
                }

                if (scopeTracker.getNumOpenBracket() == 0) {
                    if (pendingAttribute.getName() == null && pendingAttribute.getParameters().isEmpty()) {
                        setPendingItem(null);
                    } else {
                        finalizePendingItem();
                    }
                    currentAttributeState = AttributeState.SEARCH;
                    innerParsingContext = null;
                }
                break;
        }
    }
}
