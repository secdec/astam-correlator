package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.*;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import org.junit.Test;

import static com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod.AccessLevel.*;

public class CSharpMethodParserTests {

    private CSharpMethod parseMethod(String code) {
        CSharpMethodParser methodParser = new CSharpMethodParser();
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();
        CSharpParameterParser parameterParser = new CSharpParameterParser();
        CSharpAttributeParser attributeParser = new CSharpAttributeParser();

        CSharpParsingContext context = new CSharpParsingContext(attributeParser, null, methodParser, parameterParser, scopeTracker);
        methodParser.setParsingContext(context);
        parameterParser.setParsingContext(context);
        attributeParser.setParsingContext(context);

        methodParser.setClassBraceLevel(0);

        EventBasedTokenizerRunner.runString(code, new CSharpEventTokenizerConfigurator(), scopeTracker, parameterParser, attributeParser, methodParser);

        if (methodParser.hasItem()) {
            return methodParser.pullCurrentItem();
        } else {
            return null;
        }
    }

    @Test
    public void testSimpleMethod() {
        CSharpMethod method = parseMethod("void Test() {}");
        assert method.getName().equals("Test") :
            "Expected 'Test', got " + method.getName();
        assert method.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + method.getAccessLevel();
    }

    @Test
    public void testReturnType() {
        CSharpMethod method = parseMethod("string Test() {}");
        assert method.getName().equals("Test") :
            "Expected 'Test', got " + method.getName();
        assert method.getReturnType().equals("string") :
            "Expected 'string', got " + method.getReturnType();
        assert method.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + method.getAccessLevel();
    }

    @Test
    public void testGenericReturnType() {
        CSharpMethod method = parseMethod("List<string> Test() {}");
        assert method.getName().equals("Test") :
            "Expected 'Test', got " + method.getName();
        assert method.getReturnType().equals("List<string>") :
            "Expected 'List<string>', got " + method.getReturnType();
        assert method.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + method.getAccessLevel();
    }

    @Test
    public void testGenericMethod() {
        //  Generic parameter embedded in the name
        //  since we do not formally support it
        CSharpMethod method = parseMethod("void Test<T>() {}");
        assert method.getName().equals("Test<T>") :
            "Expected 'Test', got " + method.getName();
        assert method.getReturnType().equals("void") :
            "Expected 'void', got " + method.getReturnType();
        assert method.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + method.getAccessLevel();
    }

    @Test
    public void testAccessLevels() {
        CSharpMethod publicMethod = parseMethod("public void Test() {}");
        assert publicMethod.getName().equals("Test") :
            "Expected 'Test', got " + publicMethod.getName();
        assert publicMethod.getAccessLevel() == PUBLIC :
            "Expected 'PUBLIC', got " + publicMethod.getAccessLevel();

        CSharpMethod protectedMethod = parseMethod("protected void Test() {}");
        assert protectedMethod.getName().equals("Test") :
            "Expected 'Test', got " + protectedMethod.getName();
        assert protectedMethod.getAccessLevel() == PROTECTED :
            "Expected 'PROTECTED', got " + protectedMethod.getAccessLevel();

        CSharpMethod privateMethod = parseMethod("private void Test() {}");
        assert privateMethod.getName().equals("Test") :
            "Expected 'Test', got " + privateMethod.getName();
        assert privateMethod.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + privateMethod.getAccessLevel();

        CSharpMethod unlabeledMethod = parseMethod("void Test() {}");
        assert unlabeledMethod.getName().equals("Test") :
            "Expected 'Test', got " + unlabeledMethod.getName();
        assert unlabeledMethod.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + unlabeledMethod.getAccessLevel();

        //  Internal not recognized, should default to 'private'
        CSharpMethod internalMethod = parseMethod("internal void Test() {}");
        assert internalMethod.getName().equals("Test") :
            "Expected 'Test', got " + internalMethod.getName();
        assert internalMethod.getAccessLevel() == PRIVATE :
            "Expected 'PRIVATE', got " + internalMethod.getAccessLevel();
    }

    @Test
    public void testSimpleMethodParameters() {
        CSharpMethod noParameters = parseMethod("void Test() {}");
        assert noParameters.getParameters().size() == 0 :
            "Expected 0 parameters, got " + noParameters.getParameters().size();

        CSharpMethod oneParameter = parseMethod("void Test(int x) {}");
        assert oneParameter.getParameters().size() == 1 :
            "Expected 1 parameter, got " + oneParameter.getParameters().size();
        assert oneParameter.getParameter(0).getName().equals("x") :
            "Expected 'x', got " + oneParameter.getParameter(0).getName();
        assert oneParameter.getParameter(0).getType().equals("int") :
            "Expected 'int', got " + oneParameter.getParameter(0).getType();

        CSharpMethod twoParameters = parseMethod("void Test(int x, float y) {}");
        assert twoParameters.getParameters().size() == 2 :
            "Expected 2 parameters, got " + twoParameters.getParameters().size();
        assert twoParameters.getParameter(0).getName().equals("x") :
            "Expected 'x', got " + twoParameters.getParameter(0).getName();
        assert twoParameters.getParameter(0).getType().equals("int") :
            "Expected 'int', got " + twoParameters.getParameter(0).getType();
        assert twoParameters.getParameter(1).getName().equals("y") :
            "Expected 'y', got " + twoParameters.getParameter(1).getName();
        assert twoParameters.getParameter(1).getType().equals("float") :
            "Expected 'float', got " + twoParameters.getParameter(1).getType();
    }

    @Test
    public void testPropertyDefinitionUnrecognized() {
        CSharpMethod nullMethodGetSetDefault = parseMethod("int Property { get; set; }");
        assert nullMethodGetSetDefault == null :
            "Expected null, got " + nullMethodGetSetDefault;

        CSharpMethod nullMethodGetDefined = parseMethod("int Property { get { return 0; } }");
        assert nullMethodGetDefined == null :
            "Expected null, got " + nullMethodGetDefined;

        CSharpMethod nullMethodGetSetDefined = parseMethod("int Property { get { return 0; } set { x = value; } }");
        assert nullMethodGetSetDefined == null :
            "Expected null, got " + nullMethodGetSetDefined;

        CSharpMethod nullMethodArrow = parseMethod("int Property => 0;");
        assert nullMethodArrow == null :
            "Expected null, got " + nullMethodArrow;
    }

    @Test
    public void testArrowMethod() {
        CSharpMethod arrowMethodNoParams = parseMethod("int Test() => run();");
        assert arrowMethodNoParams != null :
            "Expected method, got null";
        assert arrowMethodNoParams.getParameters().size() == 0 :
            "Expected 0 params, got " + arrowMethodNoParams.getParameters().size();
        assert arrowMethodNoParams.getReturnType().equals("int") :
            "Expected 'int', got " + arrowMethodNoParams.getReturnType();

        CSharpMethod arrowMethodOneParam = parseMethod("int Test(int x) => run(x);");
        assert arrowMethodOneParam != null :
            "Expected method, got null";
        assert arrowMethodOneParam.getParameters().size() == 1 :
            "Expected 1 param, got " + arrowMethodOneParam.getParameters().size();
        assert arrowMethodOneParam.getReturnType().equals("int") :
            "Expected 'int', got " + arrowMethodOneParam.getReturnType();
    }

    @Test
    public void testMethodAttribute() {
        CSharpMethod method = parseMethod("[TestAttrib] int Test() { }");
        assert method != null :
            "Expected method, got null";
        assert method.getAttributes().size() == 1 :
            "Expected 1 attribute, got " + method.getAttributes().size();
        assert method.getAttributes().get(0).getName().equals("TestAttrib") :
            "Expected 'TestAttrib', got " + method.getAttributes().get(0).getName();
        assert method.getAttribute("TestAttrib") != null :
            "Expected attribute, got null";

        CSharpMethod manyAttributesMethod = parseMethod("[Foo, Bar] int Test() { }");
        assert manyAttributesMethod != null :
            "Expected method, got null";
        assert manyAttributesMethod.getAttributes().size() == 2 :
            "Expected 2 attributes, got " + manyAttributesMethod.getAttributes().size();
        assert manyAttributesMethod.getAttribute("Foo") != null :
            "Expected 'Foo' attribute, got null";
        assert manyAttributesMethod.getAttribute("Bar") != null :
            "Expected 'Bar' attribute, got null";
        assert manyAttributesMethod.getAttributes("Foo", "Bar").size() == 2 :
            "Expected 2 attributes, got " + manyAttributesMethod.getAttributes("Foo", "Bar").size();
        assert manyAttributesMethod.getAttributes("Foo", "Bar").get(0).getName().equals("Foo") :
            "Expected 'Foo' attribute, got " + manyAttributesMethod.getAttributes("Foo", "Bar").get(0).getName();
        assert manyAttributesMethod.getAttributes("Foo", "Bar").get(1).getName().equals("Bar") :
            "Expected 'Bar' attribute, got " + manyAttributesMethod.getAttributes("Foo", "Bar").get(1).getName();
    }

    @Test
    public void testConstructorMethod() {
        //  Constructors aren't currently supported
        CSharpMethod method = parseMethod("MyClass() {}");
        assert method == null :
            "Expected null, got " + method;
    }

}
