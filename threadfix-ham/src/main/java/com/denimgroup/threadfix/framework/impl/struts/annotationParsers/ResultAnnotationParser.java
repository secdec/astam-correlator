package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultAnnotation;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ResultAnnotationParser extends AbstractAnnotationParser {

    static SanitizedLogger LOG = new SanitizedLogger("ResultAnnotationParser");

    List<Annotation> processedAnnotations = list();
    List<ResultAnnotation> pendingAnnotations = list();

    ResultAnnotation currentAnnotation;

    @Override
    public Collection<Annotation> getAnnotations() {
        return processedAnnotations;
    }

    @Override
    protected String getAnnotationName() {
        return "Result";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentAnnotation = new ResultAnnotation();
        currentAnnotation.setCodeLine(lineNumber);
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        processedAnnotations.add(currentAnnotation);
        pendingAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType, int lineNumber) {
        for (ResultAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex, int lineNumber) {
        switch (parameterIndex) {
            case 0:
                //  Result name
                currentAnnotation.setResultName(value);
                break;

            case 1:
                //  Result location
                currentAnnotation.setResultLocation(value);
                break;

            case 2:
                //  Result type (ie redirect)
                currentAnnotation.setResultType(value);
                break;

            case 3:
                //  Parameters passed to result view
                Map<String, String> parameters = parseResultParameters(value);
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    currentAnnotation.addParameter(param.getKey(), param.getValue());
                }
                break;
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value, int lineNumber) {
        if (name.equals("name")) {
            currentAnnotation.setResultName(value);
        } else if (name.equals("location")) {
            currentAnnotation.setResultLocation(value);
        } else if (name.equals("type")) {
            currentAnnotation.setResultType(value);
        } else if (name.equals("params")) {
            Map<String, String> parameters = parseResultParameters(value);
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                currentAnnotation.addParameter(param.getKey(), param.getValue());
            }
        }
    }

    Map<String, String> parseResultParameters(String text) {
        Map<String, String> params = new HashMap<String, String>();
        String[] split = CodeParseUtil.splitByComma(text);
        for (int i = 0; i < split.length - 1; i += 2) {
            String current = split[i];
            String next = split[i + 1];
            params.put(current, next);
        }
        return params;
    }

    Map<String, String> parseResultParameters1(String text) {

        Map<String, String> params = new HashMap<String, String>();

        text = text.replace("\n", "");

        List<String> parts = list();

        boolean isInString = false;
        String currentString = "";
        for (int i = 0; i < text.length() - 1; i++) {
            char cur = text.charAt(i);
            char nxt = text.charAt(i + 1);

            if (cur == '"') {
                if (isInString) {
                    currentString += cur;
                    parts.add(currentString);
                    currentString = "";
                    isInString = false;
                } else {
                    currentString = "";
                    currentString += cur;
                    isInString = true;
                }
            } else if (isInString) {
                if (cur == '\\' && nxt == '"') {
                    i++;
                    currentString += '"';
                } else {
                    currentString += cur;
                }
            }
        }

        for (int i = 0; i < parts.size(); i += 2) {
            String label = parts.get(i);
            String value = parts.get(i + 1);

            params.put(label, value);
        }

        return params;
    }
}
