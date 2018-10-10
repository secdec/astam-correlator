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

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

public class CSharpFileParser implements EventBasedTokenizer {

    @Nonnull
    public static List<CSharpClass> parse(@Nonnull File file) {
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();

        CSharpAttributeParser attributeParser = new CSharpAttributeParser();
        CSharpParameterParser parameterParser = new CSharpParameterParser();
        CSharpMethodParser methodParser = new CSharpMethodParser();
        CSharpClassParser classParser = new CSharpClassParser();

        CSharpParsingContext parsingContext = new CSharpParsingContext(
            attributeParser, classParser, methodParser, parameterParser, scopeTracker
        );
        parsingContext.applyToParsers();

        CSharpFileParser fileParser = new CSharpFileParser(parsingContext);

        //  ORDER-DEPENDENT!
        //  Parsers consume from others in the order that they are ran
        //
        //  void Post([Attr(abc)]String param)
        //
        //  ^ attribute parser should read '(abc)' params before method parser, so attribute
        //  parser comes first

        EventBasedTokenizerRunner.run(file,
            new CSharpEventTokenizerConfigurator(),
            scopeTracker,
            fileParser,
            classParser,
            parameterParser,
            attributeParser,
            methodParser
        );

        List<CSharpClass> classes = fileParser.classes;
        for (CSharpClass cls : classes) {
            cls.setFilePath(file.getAbsolutePath());
        }

        return classes;
    }




    private CSharpParsingContext parsingContext;
    private List<CSharpClass> classes = list();

    String namespaceName;

    public CSharpFileParser(CSharpParsingContext context) {
        parsingContext = context;
    }



    private enum FileState {
        SEARCH, USING_NAMESPACE, NAMESPACE_NAME, IN_NAMESPACE
    }

    FileState currentFileState = FileState.SEARCH;



    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        CSharpScopeTracker scopeTracker = parsingContext.getScopeTracker();
        CSharpClassParser classParser = parsingContext.getClassParser();
        CSharpMethodParser methodParser = parsingContext.getMethodParser();
        CSharpParameterParser parameterParser = parsingContext.getParameterParser();
        CSharpAttributeParser attributeParser = parsingContext.getAttributeParser();

        if (scopeTracker.isInComment()) {
            return;
        }

        if (currentFileState != FileState.IN_NAMESPACE) {
            classParser.disable();
            methodParser.disable();
            parameterParser.disable();
            attributeParser.disable();
        }

        if (classParser.isBuildingItem()) {
            return;
        }

        while (classParser.hasItem()) {
            CSharpClass newClass = classParser.pullCurrentItem();
            newClass.setNamespace(namespaceName);
            classes.add(newClass);
        }

        switch (currentFileState) {
            case SEARCH:
                if (NAMESPACE.equals(stringValue)) {
                    currentFileState = FileState.NAMESPACE_NAME;
                } else if (USING.equals(stringValue)) {
                    currentFileState = FileState.USING_NAMESPACE;
                }
                break;

            case USING_NAMESPACE:
                if (type == ';') {
                    currentFileState = FileState.SEARCH;
                }
                break;

            case NAMESPACE_NAME:
                if (namespaceName == null) {
                    namespaceName = "";
                }

                if (type == '{') {
                    currentFileState = FileState.IN_NAMESPACE;
                    parameterParser.enable();
                    attributeParser.enable();
                    classParser.enable();
                } else {
                    namespaceName += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;

            case IN_NAMESPACE:
                if (scopeTracker.getNumOpenBrace() == 0) {
                    classParser.resetAll();
                    currentFileState = FileState.SEARCH;
                }
                break;
        }
    }
}
