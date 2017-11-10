package com.denimgroup.threadfix.framework.impl.django;

import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonCodeCollection {

    Map<String, Collection<String>> classMap = map();
    Map<String, Collection<String>> functionMap = map();

    private void addToMapCollection(Map<String, Collection<String>> map, String key, String value) {
        if (!map.containsKey(key)) {
            map.put(key, new LinkedList<String>());
        }

        Collection<String> classes = map.get(key);
        classes.add(value);
    }

    private void addAllToMapCollection(Map<String, Collection<String>> map, String key, Collection<String> value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        } else {
            Collection<String> existingClassNames = map.get(key);
            existingClassNames.addAll(value);
        }
    }

    public void addClass(String sourceFilePath, String className) {
        addToMapCollection(classMap, sourceFilePath, className);
    }

    public void addClasses(String sourceFilePath, Collection<String> classNames) {
        addAllToMapCollection(classMap, sourceFilePath, classNames);
    }

    public void addFunction(String sourceFilePath, String functionName) {
        addToMapCollection(functionMap, sourceFilePath, functionName);
    }

    public void addFunctions(String sourceFilePath, Collection<String> functionNames) {
        addAllToMapCollection(functionMap, sourceFilePath, functionNames);
    }

    public String findFileForClass(String className) {
        for (Map.Entry<String, Collection<String>> entry : classMap.entrySet()) {
            if (entry.getValue().contains(className)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String findFileForFunction(String functionName) {
        for (Map.Entry<String, Collection<String>> entry : functionMap.entrySet()) {
            if (entry.getValue().contains(functionName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Collection<String> findClassesForFile(String absolutePath) {
        if (!classMap.containsKey(absolutePath)) {
            return null;
        } else {
            return classMap.get(absolutePath);
        }
    }

    public Collection<String> getAllClasses() {
        List<String> classNames = list();
        for (Map.Entry<String, Collection<String>> entry : classMap.entrySet()) {
            classNames.addAll(entry.getValue());
        }
        return classNames;
    }

    public Collection<String> findFunctionsForFile(String absolutePath) {
        if (!functionMap.containsKey(absolutePath)) {
            return null;
        } else {
            return functionMap.get(absolutePath);
        }
    }

    public Collection<String> getAllFunctions() {
        List<String> functionNames = list();
        for (Map.Entry<String, Collection<String>> entry : functionMap.entrySet()) {
            functionNames.addAll(entry.getValue());
        }
        return functionNames;
    }

    public boolean containsFile(String absolutePath) {
        return classMap.containsKey(absolutePath) || functionMap.containsKey(absolutePath);
    }

    public boolean containsClass(String className) {
        for (Map.Entry<String, Collection<String>> entry : classMap.entrySet()) {
            if (entry.getValue().contains(className)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsFunction(String functionName) {
        for (Map.Entry<String, Collection<String>> entry : functionMap.entrySet()) {
            if (entry.getValue().contains(functionName)) {
                return true;
            }
        }
        return false;
    }



}
