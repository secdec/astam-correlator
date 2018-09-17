package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

public class CSharpAttributeParser extends AbstractCSharpParser<CSharpAttribute> implements EventBasedTokenizer {

    /*
     * NOTE - When parsing an attribute containing parameters, where the attribute is attached to a parameter, the attribute's
     *      parameters will be ignored. ie:
     *
     *      void Sample([Attr("foo")] int myParam)
     *
     *      `myParam` will have `Attr` attached to it, but `Attr` will not have any parameters assigned to it. This is a
     *      limitation of coupling the Attribute and Method Parser instances directly to each other.
     */

    private CSharpParameterParser parameterParser;
    private CSharpScopeTracker scopeTracker;

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



    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (isDisabled() || scopeTracker.isInComment()) {
            return;
        }

        switch (currentAttributeState) {
            case SEARCH:
                if (scopeTracker.getNumOpenBracket() == 1 && !parameterParser.isBuildingParameterType()) {
                    currentAttributeState = AttributeState.IN_ATTRIBUTE;
                    setPendingItem(new CSharpAttribute());
                    parameterParser.clearItems();
                }
                break;

            case IN_ATTRIBUTE:
                CSharpAttribute pendingAttribute = getPendingItem();
                if (stringValue != null && pendingAttribute.getName() == null) {
                    pendingAttribute.setName(stringValue);
                }

                while (parameterParser.hasItem()) {
                    pendingAttribute.addParameter(parameterParser.pullCurrentItem());
                }

                if (scopeTracker.getNumOpenBracket() == 0) {
                    if (pendingAttribute.getName() == null && pendingAttribute.getParameters().isEmpty()) {
                        setPendingItem(null);
                    } else {
                        finalizePendingItem();
                    }
                    currentAttributeState = AttributeState.SEARCH;
                }
                break;
        }
    }
}
