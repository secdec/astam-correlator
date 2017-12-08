package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

//  WARNING - Duplicated code across the codebase will resolve to the same expression for all
//  occurrences of that duplicated code - the cached expression for each occurrence will have
//  the same code location!

public class PythonCachingExpressionParser extends PythonExpressionParser {

    Map<String, PythonExpression> cache = map();

    @Override
    public PythonExpression processString(String stringValue) {
        return retrieveOrCacheExpression(stringValue, stringValue, null);
    }

    @Override
    public PythonExpression processString(String stringValue, List<PythonValue> subjects) {

        if (subjects == null) {
            return this.processString(stringValue);
        }

        StringBuilder identifier = new StringBuilder(stringValue);
        for (PythonValue subject : subjects) {
            identifier.append('|');
            identifier.append(subject.toString());
        }

        return retrieveOrCacheExpression(stringValue, identifier.toString(), subjects);
    }

    PythonExpression retrieveOrCacheExpression(String expression, String identifier, List<PythonValue> subjects) {
        PythonExpression result;
        if (cache.containsKey(identifier)) {
            result = (PythonExpression)cache.get(identifier).clone();
        } else {
            result = super.processString(expression, subjects);
            cache.put(identifier, result);
            result = (PythonExpression)result.clone();
        }
        return result;
    }
}
