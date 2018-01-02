package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonLambda;

import java.io.*;
import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoFunctionUrlsInterpreter {

    PythonCodeCollection codebase;
    String functionBody = null;
    AbstractPythonStatement context;

    public DjangoFunctionUrlsInterpreter(PythonCodeCollection codebase, AbstractPythonStatement context, String code) {
        this.functionBody = code;
        this.context = context;
        this.codebase = codebase;
    }

    public DjangoFunctionUrlsInterpreter(PythonCodeCollection codebase, AbstractPythonStatement context, File sourceFile, int startLine, int endLine) {
        try {
            FileReader fileReader = new FileReader(sourceFile);
            BufferedReader reader = new BufferedReader(fileReader);

            int line = 0;
            while (line++ < startLine) {
                reader.readLine();
            }

            StringBuilder bodyBuilder = new StringBuilder();
            while (line++ <= endLine) {
                String lineText = reader.readLine();
                if (lineText == null) {
                    break;
                } else {
                    bodyBuilder.append(lineText);
                }
            }

            functionBody = bodyBuilder.toString();
            this.codebase = codebase;
            this.context = context;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String interpret(Collection<String> paramValues) {
        Collection<PythonLambda> urlLambdas = list();
        Collection<PythonFunction> urlFunctions = list();

        return null;
    }

}
