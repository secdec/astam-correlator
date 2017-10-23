package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.dotNet.DotNetRoutesParser;
import com.denimgroup.threadfix.framework.impl.struts.annotationParsers.*;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.*;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.omg.CORBA.UNKNOWN;

import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsClassParser {

    ActionAnnotationParser actionParser = new ActionAnnotationParser();
    NamespaceAnnotationParser namespaceParser = new NamespaceAnnotationParser();
    ParentPackageAnnotationParser parentPackageParser = new ParentPackageAnnotationParser();
    ResultAnnotationParser resultParser = new ResultAnnotationParser();
    ResultPathAnnotationParser resultPathParser = new ResultPathAnnotationParser();


    StrutsClass resultClass;


    public StrutsClassParser(File file) {
        //EventBasedTokenizerRunner.run(file, actionParser, namespaceParser, parentPackageParser, resultParser, resultPathParser);
        EventBasedTokenizerRunner.run(file, true, resultParser);

        String className = file.getName().replace(".java", "");
        resultClass = new StrutsClass(className, file.getAbsolutePath());

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
