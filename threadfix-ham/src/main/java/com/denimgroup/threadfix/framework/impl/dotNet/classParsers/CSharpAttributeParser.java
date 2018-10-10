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

                //  For multiple attributes in the same brackets
                if (scopeTracker.getNumOpenParen() <= attributeEntryParenLevel && type == ',') {
                    finalizePendingItem();
                    setPendingItem(new CSharpAttribute());
                }

                if (scopeTracker.getNumOpenBracket() == 0) {
                    if (pendingAttribute.getName() == null && pendingAttribute.getParameters().isEmpty()) {
                        setPendingItem(null);
                    } else {
                        finalizePendingItem();
                    }
                    currentAttributeState = AttributeState.SEARCH;
                    innerParsingContext = null;
                    attributeEntryParenLevel = -1;
                }
                break;
        }
    }
}
