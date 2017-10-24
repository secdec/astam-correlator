package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.CodeStringUtil;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ActionAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultAnnotation;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

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
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingAnnotations.add(currentAnnotation);
        processedAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType) {

        for (ActionAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex) {
        switch (parameterIndex) {
            case 0: // value
                currentAnnotation.setBoundUrl(value);
                break;

            case 1: // results
                parseActionResults(value);
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
    protected void onNamedAnnotationParameter(String name, String value) {
        if (name.equals("value")) {
            currentAnnotation.setBoundUrl(value);
        } else if (name.equals("params")) {
            parseActionParams(value);
        } else if (name.equals("results")) {
            parseActionResults(value);
        } else if (name.equals("className")) {
            currentAnnotation.setExplicitClassName(value);
        }
    }

    private void parseActionParams(String paramsString) {
        String[] parts = CodeStringUtil.splitByComma(paramsString);
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < parts.length - 1; i += 2) {
            currentAnnotation.addParameter(parts[i], parts[i + 1]);
        }
    }

    private void parseActionResults(String resultsString) {
        String[] resultAnnotationStrings = CodeStringUtil.splitByComma(resultsString);

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
                    currentAnnotation.addResult((ResultAnnotation)resultAnnotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
