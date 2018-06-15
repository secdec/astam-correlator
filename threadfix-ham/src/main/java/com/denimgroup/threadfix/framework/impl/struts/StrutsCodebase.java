package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsCodebase {
    List<StrutsClass> classes = list();

    public void addClasses(Collection<StrutsClass> classes) {
        this.classes.addAll(classes);
    }

    public Collection<StrutsClass> getClasses() {
        return this.classes;
    }

    public StrutsClass findClassByFileLocation(String fileLocation) {
        for (StrutsClass strutsClass : classes) {
            if (strutsClass.getSourceFile().equalsIgnoreCase(fileLocation)) {
                return strutsClass;
            }
        }
        return null;
    }


    public StrutsClass findClassByName(String className) {
        for (StrutsClass strutsClass : classes) {
            if (strutsClass.getName().equalsIgnoreCase(className) ||
                (strutsClass.getPackage() + "." + strutsClass.getName()).equalsIgnoreCase(className)) {
                return strutsClass;
            }
        }
        return null;
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
