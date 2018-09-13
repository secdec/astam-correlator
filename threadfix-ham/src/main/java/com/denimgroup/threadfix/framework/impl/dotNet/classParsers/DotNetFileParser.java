package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.DotNetClass;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

public class DotNetFileParser implements EventBasedTokenizer {

    @Nonnull
    public static List<DotNetClass> parse(@Nonnull File file) {
        DotNetScopeTracker scopeTracker = new DotNetScopeTracker();

        DotNetAttributeParser attributeParser = new DotNetAttributeParser();
        DotNetParameterParser parameterParser = new DotNetParameterParser();
        DotNetMethodParser methodParser = new DotNetMethodParser();
        DotNetClassParser classParser = new DotNetClassParser();

        DotNetParsingContext parsingContext = new DotNetParsingContext(
            attributeParser, classParser, methodParser, parameterParser, scopeTracker
        );
        parsingContext.applyToParsers();

        DotNetFileParser fileParser = new DotNetFileParser(parsingContext);

        //  ORDER-DEPENDENT!
        //  Parsers consume from others in the order that they are ran
        //
        //  void Post([Attr(abc)]String param)
        //
        //  ^ attribute parser should read '(abc)' params before method parser, so attribute
        //  parser comes first

        EventBasedTokenizerRunner.run(file,
            scopeTracker,
            fileParser,
            classParser,
            parameterParser,
            attributeParser,
            methodParser
        );

        List<DotNetClass> classes = fileParser.classes;
        for (DotNetClass cls : classes) {
            cls.setFilePath(file.getAbsolutePath());
        }

        return classes;
    }




    private DotNetParsingContext parsingContext;
    private List<DotNetClass> classes = list();

    String namespaceName;

    public DotNetFileParser(DotNetParsingContext context) {
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
        DotNetScopeTracker scopeTracker = parsingContext.getScopeTracker();
        DotNetClassParser classParser = parsingContext.getClassParser();
        DotNetMethodParser methodParser = parsingContext.getMethodParser();
        DotNetParameterParser parameterParser = parsingContext.getParameterParser();
        DotNetAttributeParser attributeParser = parsingContext.getAttributeParser();

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
            DotNetClass newClass = classParser.pullCurrentItem();
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
