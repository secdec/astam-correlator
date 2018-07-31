package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;
import com.denimgroup.threadfix.framework.util.CaseInsensitiveStringMap;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.util.CollectionUtils.stringMap;

public class StrutsCodebase {
    List<StrutsClass> classes = list();
    CaseInsensitiveStringMap<StrutsClass> classesByName = stringMap();
    CaseInsensitiveStringMap<StrutsClass> classesByFullName = stringMap();
    CaseInsensitiveStringMap<StrutsClass> classesByFilePath = stringMap();

    public void addClasses(Collection<StrutsClass> classes) {
        this.classes.addAll(classes);

        for (StrutsClass strutsClass : classes) {
            classesByName.put(strutsClass.getName(), strutsClass);
            classesByFullName.put(strutsClass.getPackage() + "." + strutsClass.getName(), strutsClass);
            classesByFilePath.put(strutsClass.getSourceFile(), strutsClass);
        }
    }

    public Collection<StrutsClass> getClasses() {
        return this.classes;
    }

    public StrutsClass findClassByFileLocation(String fileLocation) {
        return classesByFilePath.get(fileLocation);
    }


    public StrutsClass findClassByName(String className) {
        StrutsClass result = classesByFullName.get(className);
        if (result == null) {
            result = classesByName.get(className);
        }

        return result;
    }

    public StrutsMethod findMethodByCodeLines(String sourceFile, int lineNumber) {
        StrutsClass strutsClass = findClassByFileLocation(sourceFile);
        if (strutsClass != null) {
            for (StrutsMethod method : strutsClass.getMethods()) {
                if (method.getStartLine() <= lineNumber && method.getEndLine() >= lineNumber) {
                    return method;
                }
            }
        }

        return null;
    }
}
