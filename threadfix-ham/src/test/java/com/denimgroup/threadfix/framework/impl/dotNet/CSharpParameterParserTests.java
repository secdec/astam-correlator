package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.*;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import org.junit.Test;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpParameterParserTests {

    private List<CSharpParameter> parseParameters(String sourceCode) {
        CSharpAttributeParser attributeParser = new CSharpAttributeParser();
        CSharpParameterParser parameterParser = new CSharpParameterParser();
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();
        CSharpParsingContext parsingContext = new CSharpParsingContext(attributeParser, null, null, parameterParser, scopeTracker);

        attributeParser.setParsingContext(parsingContext);
        parameterParser.setParsingContext(parsingContext);

        EventBasedTokenizerRunner.runString(sourceCode, new CSharpEventTokenizerConfigurator(), scopeTracker, parameterParser, attributeParser);

        List<CSharpParameter> parsedParameters = list();
        while (parameterParser.hasItem()) {
            parsedParameters.add(parameterParser.pullCurrentItem());
        }
        return parsedParameters;
    }

    @Test
    public void testNoParameters() {
        List<CSharpParameter> params = parseParameters("()");
        assert params.size() == 0 :
            "Expected 0 params, got " + params.size();
    }

    @Test
    public void testParameterDeclaration() {
        CSharpParameter param = parseParameters("(int x)").get(0);
        assert param.getName().equals("x") :
            "Expected 'x', got " + param.getName();
        assert param.getType().equals("int") :
            "Expected 'int', got " + param.getType();
    }

    @Test
    public void testMultiParameterDeclaration() {
        List<CSharpParameter> params = parseParameters("(int x, float y)");
        assert params.size() == 2 :
            "Expected 2 params, got " + params.size();

        CSharpParameter first = params.get(0);
        CSharpParameter second = params.get(1);

        assert first.getName().equals("x") :
            "Expected 'x', got " + first.getName();

        assert first.getType().equals("int") :
            "Expected 'int', got " + first.getType();

        assert second.getName().equals("y") :
            "Expected 'y', got " + second.getName();

        assert second.getType().equals("float") :
            "Expected 'float', got " + second.getType();
    }

    @Test
    public void testArrayParameterDeclaration() {
        CSharpParameter param = parseParameters("(int[] x)").get(0);
        assert param.getName().equals("x") :
            "Expected 'x', got " + param.getName();
        assert param.getType().equals("int[]") :
            "Expected 'int[]', got " + param.getType();
    }

    @Test
    public void testGenericParameterDeclaration() {
        CSharpParameter param = parseParameters("(List<string> x)").get(0);
        assert param.getName().equals("x") :
            "Expected 'x', got " + param.getName();
        assert param.getType().equals("List<string>") :
            "Expected 'List<string>', got " + param.getType();
    }

    @Test
    public void testMultiGenericParameterDeclaration() {
        CSharpParameter param = parseParameters("(Dictionary<string,List<int>> x)").get(0);
        assert param.getName().equals("x") :
            "Expected 'x', got " + param.getName();
        assert param.getType().equals("Dictionary<string,List<int>>") :
            "Expected 'Dictionary<string,List<int>>', got " + param.getType();
    }

    @Test
    public void testParameterValue() {
        CSharpParameter param = parseParameters("(x)").get(0);
        assert param.getValue().equals("x") :
            "Expected 'x', got " + param.getName();
        assert param.getType() == null :
            "Expected 'null', got " + param.getType();
        assert param.getName() == null :
            "Expected 'null', got " + param.getName();
    }

    @Test
    public void testMultipleParameterValues() {
        List<CSharpParameter> params = parseParameters("(x, y)");
        assert params.size() == 2 :
            "Expected 2 params, got " + params.size();

        CSharpParameter x = params.get(0);
        CSharpParameter y = params.get(1);

        assert x.getValue().equals("x") :
            "Expected 'x', got " + x.getValue();
        assert x.getName() == null :
            "Expected 'null', got " + x.getName();

        assert y.getValue().equals("y") :
            "Expected 'y', got " + y.getValue();
        assert y.getName() == null :
            "Expected 'null', got " + y.getName();
    }

    @Test
    public void testDefaultParameterValue() {
        List<CSharpParameter> params = parseParameters("(string x, string y = \"foo\")");
        assert params.size() == 2 :
            "Expected 2 params, got " + params.size();

        CSharpParameter x = params.get(0);
        CSharpParameter y = params.get(1);

        assert x.getName().equals("x") :
            "Expected 'x', got " + x.getName();
        assert x.getType().equals("string") :
            "Expected 'string', got " + x.getType();
        assert x.getDefaultValue() == null :
            "Expected 'null', got " + x.getDefaultValue();

        assert y.getName().equals("y") :
            "Expected 'y', got " + y.getName();
        assert y.getType().equals("string") :
            "Expected 'string', got " + y.getType();
        assert y.getDefaultValue().equals("\"foo\"") :
            "Expected '\"foo\"', got " + y.getDefaultValue();
    }

    @Test
    public void testNamedParameterValue() {
        List<CSharpParameter> params = parseParameters("(x, foo: bar)");
        assert params.size() == 2 :
            "Expected 2 params, got " + params.size();

        CSharpParameter x = params.get(0);
        CSharpParameter foo = params.get(1);

        assert x.getValue().equals("x") :
            "Expected 'x', got " + x.getValue();
        assert x.getName() == null :
            "Expected 'null', got " + x.getName();

        assert foo.getValue().equals("bar") :
            "Expected 'bar', got " + foo.getValue();
        assert foo.getName().equals("foo") :
            "Expected 'foo', got " + foo.getName();
        assert foo.getType() == null :
            "Expected 'null', got " + foo.getType();
    }

    @Test
    public void testNamedAttributePropertyValue() {
        List<CSharpParameter> params = parseParameters("(x, Foo = bar)");
        assert params.size() == 2 :
            "Expected 2 params, got " + params.size();

        CSharpParameter x = params.get(0);
        CSharpParameter foo = params.get(1);

        assert x.getName() == null :
            "Expected 'null', got " + x.getName();
        assert x.getValue().equals("x") :
            "Expected 'x', got " + x.getValue();

        assert foo.getName().equals("Foo") :
            "Expected 'Foo', got " + foo.getName();
        assert foo.getValue().equals("bar") :
            "Expected 'bar', got " + foo.getValue();
        assert foo.getType() == null :
            "Expected 'null', got " + foo.getType();
    }

}
