package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonCodeCollection {

    Map<String, Collection<PythonClass>> classMap = map();
    Map<String, Collection<PythonFunction>> functionMap = map();

    public void addClass(String sourceFilePath, PythonClass pythonClass) {
        if (!classMap.containsKey(sourceFilePath)) {
            classMap.put(sourceFilePath, new LinkedList<PythonClass>());
        }
        Collection<PythonClass> classes = classMap.get(sourceFilePath);
        classes.add(pythonClass);
    }

    public void addClasses(String sourceFilePath, Collection<PythonClass> classes) {
        if (!classMap.containsKey(sourceFilePath)) {
            classMap.put(sourceFilePath, classes);
        } else {
            Collection<PythonClass> existingClasses = classMap.get(sourceFilePath);
            existingClasses.addAll(classes);
        }
    }

    public void addFunction(String sourceFilePath, PythonFunction pythonFunction) {
        if (!functionMap.containsKey(sourceFilePath)) {
            functionMap.put(sourceFilePath, new LinkedList<PythonFunction>());
        }
        Collection<PythonFunction> classes = functionMap.get(sourceFilePath);
        classes.add(pythonFunction);
    }

    public void addFunctions(String sourceFilePath, Collection<PythonFunction> functions) {
        if (!functionMap.containsKey(sourceFilePath)) {
            functionMap.put(sourceFilePath, functions);
        } else {
            Collection<PythonFunction> existingFunctions = functionMap.get(sourceFilePath);
            existingFunctions.addAll(functions);
        }
    }

    public String findFileForClass(String className) {
        for (Map.Entry<String, Collection<PythonClass>> entry : classMap.entrySet()) {
            for (PythonClass pythonClass : entry.getValue()) {
                if (pythonClass.getName().equals(className))
                    return entry.getKey();
            }
        }
        return null;
    }

    public String findFileForFunction(String functionName) {
        for (Map.Entry<String, Collection<PythonFunction>> entry : functionMap.entrySet()) {
            for (PythonFunction pythonFunction : entry.getValue()) {
                if (pythonFunction.getName().equals(functionName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public Collection<PythonClass> findClassesForFile(String absolutePath) {
        if (!classMap.containsKey(absolutePath)) {
            return null;
        } else {
            return classMap.get(absolutePath);
        }
    }

    public Collection<PythonClass> getAllClasses() {
        List<PythonClass> classes = list();
        for (Map.Entry<String, Collection<PythonClass>> entry : classMap.entrySet()) {
            classes.addAll(entry.getValue());
        }
        return classes;
    }

    public Collection<PythonFunction> findFunctionsForFile(String absolutePath) {
        if (!functionMap.containsKey(absolutePath)) {
            return null;
        } else {
            return functionMap.get(absolutePath);
        }
    }

    public Collection<PythonFunction> getAllGlobalFunctions() {
        List<PythonFunction> functions = list();
        for (Map.Entry<String, Collection<PythonFunction>> entry : functionMap.entrySet()) {
            functions.addAll(entry.getValue());
        }
        return functions;
    }

    public boolean containsFile(String absolutePath) {
        return classMap.containsKey(absolutePath) || functionMap.containsKey(absolutePath);
    }

    public boolean containsClass(String className) {
        for (Map.Entry<String, Collection<PythonClass>> entry : classMap.entrySet()) {
            for (PythonClass pythonClass : entry.getValue()) {
                if (pythonClass.getName().equals(className))
                    return true;
            }
        }
        return false;
    }

    public boolean containsGlobalFunction(String functionName) {
        for (Map.Entry<String, Collection<PythonFunction>> entry : functionMap.entrySet()) {
            for (PythonFunction pythonFunction : entry.getValue()) {
                if (pythonFunction.getName().equals(functionName))
                    return true;
            }
        }
        return false;
    }



}
