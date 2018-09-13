package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.DotNetAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.DotNetParameter;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DotNetAttributeParser extends AbstractDotNetParser<DotNetAttribute> implements EventBasedTokenizer {

    /*
     * NOTE - When parsing an attribute containing parameters, where the attribute is attached to a parameter, the attribute's
     *      parameters will be ignored. ie:
     *
     *      void Sample([Attr("foo")] int myParam)
     *
     *      `myParam` will have `Attr` attached to it, but `Attr` will not have any parameters assigned to it. This is a
     *      limitation of coupling the Attribute and Method Parser instances directly to each other.
     */

    private DotNetParameterParser parameterParser;
    private DotNetScopeTracker scopeTracker;

    @Override
    public void setParsingContext(DotNetParsingContext context) {
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
        if (isDisabled()) {
            return;
        }

        switch (currentAttributeState) {
            case SEARCH:
                if (scopeTracker.getNumOpenBracket() == 1 && !parameterParser.isBuildingParameterType()) {
                    currentAttributeState = AttributeState.IN_ATTRIBUTE;
                    setPendingItem(new DotNetAttribute());
                    parameterParser.clearItems();
                }
                break;

            case IN_ATTRIBUTE:
                DotNetAttribute pendingAttribute = getPendingItem();
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
