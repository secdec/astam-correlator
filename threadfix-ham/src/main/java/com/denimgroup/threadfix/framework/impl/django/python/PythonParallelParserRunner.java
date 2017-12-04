package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.PythonTokenizerConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import java.io.File;
import java.util.concurrent.Callable;

public class PythonParallelParserRunner implements Callable<PythonModule> {

    File targetFile;
    String moduleName;

    public PythonParallelParserRunner(File targetFile) {
        this.targetFile = targetFile;
        this.moduleName = PythonSyntaxParser.makeModuleName(targetFile);
    }

    public File getTargetFile() {
        return targetFile;
    }

    @Override
    public PythonModule call() throws Exception {
        PythonModule resultModule = new PythonModule();
        resultModule.setName(moduleName);
        resultModule.setSourceCodePath(targetFile.getAbsolutePath());

        PythonSyntaxParser parser = new PythonSyntaxParser(resultModule);
        EventBasedTokenizerRunner.run(this.targetFile, PythonTokenizerConfigurator.INSTANCE, parser);
        return resultModule;
    }
}
