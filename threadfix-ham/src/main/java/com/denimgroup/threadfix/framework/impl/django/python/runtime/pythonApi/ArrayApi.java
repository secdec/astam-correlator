package com.denimgroup.threadfix.framework.impl.django.python.runtime.pythonApi;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.InterpreterUtil;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonArray;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

// Some scaffolding for future features

public class ArrayApi implements PythonApiProvider {

    Map<String, PythonFunction> resolverMap = map();


    public ArrayApi() {
        resolverMap.put("len",
                new LenFunction());

        resolverMap.put("append",
                new AppendFunction());

        resolverMap.put("count",
                new CountFunction());

        resolverMap.put("extend",
                new ExtendFunction());

        resolverMap.put("index",
                new IndexFunction());

        resolverMap.put("insert",
                new InsertFunction());

        resolverMap.put("pop",
                new PopFunction());

        resolverMap.put("remove",
                new RemoveFunction());
    }


    @Override
    public boolean acceptsSubject(PythonValue subject) {
        return subject instanceof PythonArray;
    }

    @Override
    public boolean acceptsSubjectOperand(PythonValue subject, PythonValue operand) {
        return false;
    }

    @Override
    public AbstractPythonStatement resolveStatement(PythonValue subject, PythonValue operand, String memberSymbol) {
        return resolverMap.get(memberSymbol);
    }


    //  FUNCTIONS

    public class LenFunction extends PythonFunction {

        @Override
        public String getName() {
            return "len";
        }

        @Override
        public boolean canInvoke() {
            return true;
        }

        @Override
        public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonValue target, PythonValue[] params) {
            return super.invoke(codebase, context, target, params);
        }
    }



    // METHODS

    public class AppendFunction extends PythonFunction {
        @Override
        public String getName() {
            return "append";
        }
    }

    public class CountFunction extends PythonFunction {
        @Override
        public String toString() {
            return "count";
        }
    }

    public class ExtendFunction extends PythonFunction {
        @Override
        public String getName() {
            return "extend";
        }
    }

    public class IndexFunction extends PythonFunction {
        @Override
        public String getName() {
            return "index";
        }
    }

    public class InsertFunction extends PythonFunction {
        @Override
        public String getName() {
            return "insert";
        }
    }

    public class PopFunction extends PythonFunction {
        @Override
        public String getName() {
            return "pop";
        }
    }

    public class RemoveFunction extends PythonFunction {
        @Override
        public String getName() {
            return "remove";
        }
    }

}
