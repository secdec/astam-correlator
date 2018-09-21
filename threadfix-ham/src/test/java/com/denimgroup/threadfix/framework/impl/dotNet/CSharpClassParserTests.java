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

public class CSharpClassParserTests {

    private List<CSharpClass> parseClasses(String fileName) {
        return CSharpFileParser.parse(ResourceManager.getDotNetMvcFile(fileName));
    }

    private CSharpClass parseClassString(String code) {
        CSharpClassParser classParser = new CSharpClassParser();
        CSharpMethodParser methodParser = new CSharpMethodParser();
        CSharpParameterParser parameterParser = new CSharpParameterParser();
        CSharpAttributeParser attributeParser = new CSharpAttributeParser();
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();

        CSharpParsingContext context = new CSharpParsingContext(attributeParser, classParser, methodParser, parameterParser, scopeTracker);
        classParser.setParsingContext(context);
        methodParser.setParsingContext(context);
        parameterParser.setParsingContext(context);
        attributeParser.setParsingContext(context);

        EventBasedTokenizerRunner.runString(code, new CSharpEventTokenizerConfigurator(),
            scopeTracker,
            classParser,
            parameterParser,
            attributeParser,
            methodParser
        );

        return classParser.pullCurrentItem();
    }

    @Test
    public void testBasicControllerClassParsing() {
        CSharpClass chatClass = parseClasses("ChatController.cs").get(0);
        assert chatClass.getName().equals("ChatController") :
            "Class name was not ChatController. Got " + chatClass.getName();

        assert chatClass.getNamespace().equals("SignalRChat.Controllers") :
            "Namespace was not SignalRChat.Controllers. Got " + chatClass.getNamespace();

        assert chatClass.getBaseTypes().size() == 1 :
            "Expected one base type, got " + chatClass.getBaseTypes().size();

        assert chatClass.getBaseTypes().contains("Controller") :
            "Class did not contain Controller base type";
    }

    @Test
    public void testMultiClassParsing() {
        List<CSharpClass> classes = parseClasses("AccountViewModels.cs");
        assert classes.size() == 4 :
            "Expected 4 classes, got " + classes.size();

        List<String> expectedClassNames = list(
            "ExternalLoginConfirmationViewModel",
            "ManageUserViewModel",
            "LoginViewModel",
            "RegisterViewModel"
        );

        List<String> retrievedClassNames = list();
        for (CSharpClass cls : classes) {
            retrievedClassNames.add(cls.getName());

            assert expectedClassNames.contains(cls.getName()) :
                "Unexpected class name " + cls.getName();
        }

        for (String expectedName : expectedClassNames) {
            assert retrievedClassNames.contains(expectedName) :
                "Expected class " + expectedName + " but the class was not found";
        }
    }

    @Test
    public void testBaseClassParsing() {
        CSharpClass simpleBase = parseClassString("class Test : Base {}");
        assert simpleBase.getBaseTypes().size() == 1 :
            "Expected 1 base type, got " + simpleBase.getBaseTypes().size();
        assert simpleBase.getBaseTypes().get(0).equals("Base") :
            "Expected 'Base', got " + simpleBase.getBaseTypes().get(0);

        CSharpClass genericBase = parseClassString("class Test : Base<T> {}");
        assert genericBase.getBaseTypes().size() == 1 :
            "Expected 1 base type, got " + genericBase.getBaseTypes().size();
        assert genericBase.getBaseTypes().get(0).equals("Base<T>") :
            "Expected 'Base<T>', got " + genericBase.getBaseTypes().get(0);

        CSharpClass genericWithConstraints = parseClassString("class Test<T> : Base<T> where T : class, new() {}");
        assert genericWithConstraints.getTemplateParameterNames().size() == 1 :
            "Expected 1 template parameter, got " + genericWithConstraints.getTemplateParameterNames().size();
        assert genericWithConstraints.getBaseTypes().size() == 1 :
            "Expected 1 base type, got " + genericWithConstraints.getBaseTypes().size();
        assert genericWithConstraints.getBaseTypes().get(0).equals("Base<T>") :
            "Expected 'Base<T>', got " + genericWithConstraints.getBaseTypes().get(0);

        CSharpClass multipleBaseTypes = parseClassString("class Test : Foo, Bar {}");
        assert multipleBaseTypes.getBaseTypes().size() == 2 :
            "Expected 2 base types, got " + multipleBaseTypes.getBaseTypes().size();
        assert multipleBaseTypes.getBaseTypes().get(0).equals("Foo") :
            "Expected 'Foo', got " + multipleBaseTypes.getBaseTypes().get(0);
        assert multipleBaseTypes.getBaseTypes().get(1).equals("Bar") :
            "Expected 'Bar', got " + multipleBaseTypes.getBaseTypes().get(1);
    }
}
