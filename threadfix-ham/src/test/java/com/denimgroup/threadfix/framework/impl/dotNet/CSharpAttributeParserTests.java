package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.ResourceManager;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.*;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import org.junit.Test;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpAttributeParserTests {

    private List<CSharpClass> parseClasses(String fileName) {
        return CSharpFileParser.parse(ResourceManager.getDotNetMvcFile(fileName));
    }

    private List<CSharpAttribute> parseAttributes(String sourceCode) {
        CSharpAttributeParser attributeParser = new CSharpAttributeParser();
        CSharpParameterParser parameterParser = new CSharpParameterParser();
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();
        CSharpParsingContext parsingContext = new CSharpParsingContext(attributeParser, null, null, parameterParser, scopeTracker);

        attributeParser.setParsingContext(parsingContext);
        parameterParser.setParsingContext(parsingContext);

        EventBasedTokenizerRunner.runString(sourceCode, new CSharpEventTokenizerConfigurator(), scopeTracker, parameterParser, attributeParser);

        List<CSharpAttribute> parsedAttributes = list();
        while (attributeParser.hasItem()) {
            parsedAttributes.add(attributeParser.pullCurrentItem());
        }
        return parsedAttributes;
    }

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
    public void testSingleAttribute() {
        List<CSharpAttribute> attributes = parseAttributes("[TestAttribute]");
        assert attributes.size() == 1 :
            "Expected 1 attribute, got " + attributes.size();

        CSharpAttribute attribute = attributes.get(0);
        assert attribute.getName().equals("TestAttribute") :
            "Expected 'TestAttribute', got " + attribute.getName();
    }

    @Test
    public void testSeparatedAttributes() {
        List<CSharpAttribute> attributes = parseAttributes("[Foo]\n[Bar]");
        assert attributes.size() == 2 :
            "Expected 2 attributes, got " + attributes.size();

        CSharpAttribute foo = attributes.get(0);
        CSharpAttribute bar = attributes.get(1);

        assert foo.getName().equals("Foo") :
            "Expected 'Foo', got " + foo.getName();

        assert bar.getName().equals("Bar") :
            "Expected 'Bar', got " + bar.getName();
    }

    @Test
    public void testCombinedAttributes() {
        List<CSharpAttribute> attributes = parseAttributes("[Foo, Bar]");
        assert attributes.size() == 2 :
            "Expected 2 attributes, got " + attributes.size();

        CSharpAttribute foo = attributes.get(0);
        CSharpAttribute bar = attributes.get(1);

        assert foo.getName().equals("Foo") :
            "Expected 'Foo', got " + foo.getName();

        assert bar.getName().equals("Bar") :
            "Expected 'Bar', got " + bar.getName();
    }

    @Test
    public void testAttributeParameters() {
        CSharpAttribute attribute = parseAttributes("[Test(foo)]").get(0);
        assert attribute.getName().equals("Test") :
            "Expected 'Test', got " + attribute.getName();

        assert attribute.getParameters().size() == 1 :
            "Expected 1 parameter, got " + attribute.getParameters().size();

        CSharpParameter param = attribute.getParameters().get(0);
        assert param.getValue().equals("foo") :
            "Expected 'foo', got " + param.getValue();
    }

    @Test
    public void testAttributeMultipleParameters() {
        CSharpAttribute attribute = parseAttributes("[Test(foo, \"bar\")]").get(0);
        assert attribute.getName().equals("Test") :
            "Expected 'Test', got " + attribute.getName();

        assert attribute.getParameters().size() == 2 :
            "Expected 2 parameters, got " + attribute.getParameters().size();

        CSharpParameter foo = attribute.getParameters().get(0);
        assert foo.getValue().equals("foo") :
            "Expected 'foo', got " + foo.getValue();

        CSharpParameter bar = attribute.getParameters().get(1);
        assert bar.getValue().equals("\"bar\"") :
            "Expected '\"bar\"', got " + bar.getValue();
    }

    @Test
    public void testAttributeNamedParameters() {
        CSharpAttribute attribute = parseAttributes("[Test(val: foo, bar, Baz = \"baz\")]").get(0);
        assert attribute.getParameters().size() == 3 :
            "Expected 3 parameters, got " + attribute.getParameters().size();

        CSharpParameter val = attribute.getParameterValue("val");
        CSharpParameter bar = attribute.getParameterValue(1);
        CSharpParameter baz = attribute.getParameterValue("Baz");

        assert val.getValue().equals("foo") :
            "Expected 'foo', got " + val.getValue();
        assert bar.getValue().equals("bar") :
            "Expected 'bar', got " + bar.getValue();
        assert baz.getValue().equals("\"baz\"") :
            "Expected '\"baz\"', got " + baz.getValue();
    }

    @Test
    public void testParameterAttributes() {
        CSharpParameter parameter = parseParameters("([Test]String param)").get(0);
        assert parameter.getName().equals("param") :
            "Expected 'param', got " + parameter.getName();
        assert parameter.getAttributes().size() == 1 :
            "Expected 1 attribute, got " + parameter.getAttributes().size();

        CSharpAttribute attribute = parameter.getAttributes().get(0);
        assert attribute.getName().equals("Test") :
            "Expected 'Test', got " + attribute.getName();
        assert attribute.getParameters().size() == 0 :
            "Expected 0 attribute parameters, got " + attribute.getParameters().size();
    }

    @Test
    public void testParameterAttributesWithParameters() {
        CSharpParameter parameter = parseParameters("([Test(Val=foo)]String param)").get(0);
        assert parameter.getName().equals("param") :
            "Expected 'param', got " + parameter.getName();
        assert parameter.getAttributes().size() == 1 :
            "Expected 1 attribute, got " + parameter.getAttributes().size();

        CSharpAttribute attribute = parameter.getAttributes().get(0);
        assert attribute.getName().equals("Test") :
            "Expected 'Test', got " + attribute.getName();
        assert attribute.getParameters().size() == 1 :
            "Expected 1 attribute parameter, got " + attribute.getParameters().size();

        CSharpParameter attributeParam = attribute.getParameters().get(0);
        assert attributeParam.getName().equals("Val") :
            "Expected 'Val', got " + attributeParam.getName();
        assert attributeParam.getValue().equals("foo") :
            "Expected 'foo', got " + attributeParam.getValue();
    }

    @Test
    public void testClassAttribute() {
        CSharpClass cls = parseClasses("AttributesController.cs").get(0);
        assert cls.getAttributes().size() == 2 :
            "Expected 2 attributes, got " + cls.getAttributes().size();

        //  Attributes are pulled from CSharpAttributeParser in reverse order
        CSharpAttribute firstAttribute = cls.getAttributes().get(1);
        CSharpAttribute secondAttribute = cls.getAttributes().get(0);

        assert firstAttribute.getName().equals("Authorize") :
            "Expected attribute 1 to be 'Authorize', got " + firstAttribute.getName();
        assert secondAttribute.getName().equals("InitializeSimpleMembership") :
            "Expected attribute 2 to be 'InitializeSimpleMembership', got " + secondAttribute.getName();

        assert firstAttribute.getParameters().isEmpty() :
            "Expected Authorize attribute to have 0 parameters, got " + firstAttribute.getParameters().size();
        assert secondAttribute.getParameters().isEmpty() :
            "Expected InitializeSimpleMembership attribute to have 0 parameters, got " + secondAttribute.getParameters().size();
    }
}
