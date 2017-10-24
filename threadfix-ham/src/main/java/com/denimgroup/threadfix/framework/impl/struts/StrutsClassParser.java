package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.annotationParsers.*;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import java.io.File;

public class StrutsClassParser {

    ActionAnnotationParser actionParser = new ActionAnnotationParser();
    NamespaceAnnotationParser namespaceParser = new NamespaceAnnotationParser();
    ParentPackageAnnotationParser parentPackageParser = new ParentPackageAnnotationParser();
    ResultAnnotationParser resultParser = new ResultAnnotationParser();
    ResultPathAnnotationParser resultPathParser = new ResultPathAnnotationParser();


    StrutsClass resultClass;


    public StrutsClassParser(File file) {

        EventBasedTokenizerRunner.run(file, true, actionParser, namespaceParser, parentPackageParser, resultParser, resultPathParser);
        //EventBasedTokenizerRunner.run(file, true, actionParser);

        String className = file.getName().replace(".java", "");
        resultClass = new StrutsClass(className, file.getAbsolutePath());

        /*
            There's currently a bug where annotation parameters ie @Action(..., @Result(..))
            gets parsed twice - once by the ActionAnnotationParser and then by
            the ResultAnnotationParser. Currently a non-issue since the resulting
            incorrect endpoints won't be matched unless there are corresponding
            endpoints in a finding (unlikely)
         */

        resultClass.addAllAnnotations(actionParser.getAnnotations());
        resultClass.addAllAnnotations(namespaceParser.getAnnotations());
        resultClass.addAllAnnotations(parentPackageParser.getAnnotations());
        resultClass.addAllAnnotations(resultParser.getAnnotations());
        resultClass.addAllAnnotations(resultPathParser.getAnnotations());
    }

    public StrutsClass getResultClass() {
        return resultClass;
    }
}
