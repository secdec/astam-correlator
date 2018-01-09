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

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ActionAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultAnnotation;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ActionAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> processedAnnotations = list();
    List<ActionAnnotation> pendingAnnotations = list();
    ActionAnnotation currentAnnotation;

    public Collection<Annotation> getAnnotations() {
        return processedAnnotations;
    }

    @Override
    protected String getAnnotationName() {
        return "Action";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentAnnotation = new ActionAnnotation();
        currentAnnotation.setCodeLine(lineNumber);
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingAnnotations.add(currentAnnotation);
        processedAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType, int lineNumber) {

        for (ActionAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex, int lineNumber) {
        switch (parameterIndex) {
            case 0: // value
                currentAnnotation.setBoundUrl(value);
                break;

            case 1: // results
                parseActionResults(value, lineNumber);
                break;

            case 2: // interceptorRefs
                break;

            case 3: // params
                parseActionParams(value);
                break;

            case 4: // exceptionMappings
                break;

            case 5: // className
                currentAnnotation.setExplicitClassName(value);
                break;
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value, int lineNumber) {
        if (name.equals("value")) {
            currentAnnotation.setBoundUrl(value);
        } else if (name.equals("params")) {
            parseActionParams(value);
        } else if (name.equals("results")) {
            parseActionResults(value, lineNumber);
        } else if (name.equals("className")) {
            currentAnnotation.setExplicitClassName(value);
        }
    }

    private void parseActionParams(String paramsString) {
        String[] parts = CodeParseUtil.splitByComma(paramsString);
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < parts.length - 1; i += 2) {
            currentAnnotation.addParameter(parts[i], parts[i + 1]);
        }
    }

    private void parseActionResults(String resultsString, int lineNumber) {
        String[] resultAnnotationStrings = CodeParseUtil.splitByComma(resultsString);

        for (String annotationString : resultAnnotationStrings) {

            Reader reader = new InputStreamReader(new ByteArrayInputStream(annotationString.getBytes()));
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            tokenizer.slashSlashComments(true);
            tokenizer.slashStarComments(true);
            tokenizer.ordinaryChar('<');
            tokenizer.wordChars(':', ':');

            ResultAnnotationParser parser = new ResultAnnotationParser();

            try {
                while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                    parser.processToken(tokenizer.ttype, tokenizer.lineno(), tokenizer.sval);
                }

                for (Annotation resultAnnotation : parser.getAnnotations()) {
                    resultAnnotation.setCodeLine(lineNumber);
                    currentAnnotation.addResult((ResultAnnotation)resultAnnotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
