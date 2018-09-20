////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.ResourceManager;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspStandardApiActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspStandardMvcActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.CSharpFileParser;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetControllerParserTests {

    //  Commented these tests for now since the DotNetControllerParser is now superceded by
    //  C# class parsing and DotNetMappings*Generators

    @Nonnull
    private DotNetControllerMappings getMvcStandardControllerMappings(String fileName) {
        List<CSharpClass> fileClasses = CSharpFileParser.parse(ResourceManager.getDotNetMvcFile(fileName));
        Map<String, RouteParameterMap> fileParams = map(fileName, new RouteParameterMap());

        CSharpClass mainClass = fileClasses.get(0);
        if (!mainClass.getBaseTypes().contains("Controller")) {
            mainClass.getBaseTypes().add("Controller");
        }

        AspActionGenerator actionGenerator = new AspStandardMvcActionGenerator(fileClasses, fileParams);
        return actionGenerator.generate().get(0);
    }

    @Nonnull
    private DotNetControllerMappings getApiStandardControllerMappings(String fileName) {
        List<CSharpClass> fileClasses = CSharpFileParser.parse(ResourceManager.getDotNetMvcFile(fileName));
        Map<String, RouteParameterMap> fileParams = map(fileName, new RouteParameterMap());

        CSharpClass mainClass = fileClasses.get(0);
        if (!mainClass.getBaseTypes().contains("ApiController")) {
            mainClass.getBaseTypes().add("ApiController");
        }

        AspActionGenerator actionGenerator = new AspStandardApiActionGenerator(fileClasses, fileParams);
        return actionGenerator.generate().get(0);
    }

    @Test
    public void testBasicController() {
        DotNetControllerMappings mappings = getMvcStandardControllerMappings("ChatController.cs");

        assert mappings.getControllerName() != null :
                "Controller name was null.";
        assert mappings.getControllerName().equals("Chat") :
                "Controller name was " + mappings.getControllerName() + " but should have been Chat.";
        assert mappings.getActions().size() == 1 :
                "The size was " + mappings.getActions().size() + " instead of 1.";
        assert mappings.getActionForNameAndMethod("Index", "GET") != null :
                "Mappings didn't contain Index. They had " + mappings.getActions().iterator().next();
    }
//
    @Test
    public void testControllerWithPostAttribute() {
        DotNetControllerMappings mappings = getMvcStandardControllerMappings("AttributesController.cs");

        assert mappings.getControllerName() != null :
                "Controller name was null.";
        assert mappings.getControllerName().equals("Account") :
                "Controller name was " + mappings.getControllerName() + " but should have been Account.";
        assert mappings.getActionForNameAndMethod("Login", "POST") != null :
                "Mappings didn't contain Login with POST.";
    }

    @Test
    public void testRestController() {
        DotNetControllerMappings mappings = getApiStandardControllerMappings("RestController.cs");

        assert mappings.getControllerName() != null :
                "Controller name was null.";
        assert mappings.getControllerName().equals("Students") :
                "Controller name was " + mappings.getControllerName() + " but should have been Students.";
        assert mappings.getActionForNameAndMethod("Get", "GET") != null :
                "Mappings didn't contain Get with GET.";
    }

    @Test
    public void testAttributesControllerActionSizeAndMethods() {
        DotNetControllerMappings mappings = getMvcStandardControllerMappings("AttributesController.cs");

        List<String> expectedActions = Arrays.asList(
                "Login",
                "Login",
                "LogOff",
                "Register",
                "Register",
                "Disassociate",
                "Manage",
                "Manage",
                "ExternalLogin",
                "ExternalLoginCallback",
                "ExternalLoginConfirmation",
                "ExternalLoginFailure",
                "ExternalLoginsList",
                "RemoveExternalLogins"
        ), expectedMethods = Arrays.asList(
                "GET",
                "POST",
                "POST",
                "GET",
                "POST",
                "POST",
                "GET",
                "POST",
                "POST",
                "GET",
                "POST",
                "GET",
                "GET",
                "GET"
        ), missing = list(), extra = list();

        assert expectedActions.size() == expectedMethods.size() :
                "Expected actions and methods didn't match up.";
        assert mappings.getControllerName() != null :
                "Controller name was null.";
        assert mappings.getControllerName().equals("Account") :
                "Controller name was " + mappings.getControllerName() + " but should have been Account.";


        for (int i = 0; i < expectedActions.size(); i++) {
            if (mappings.getActionForNameAndMethod(expectedActions.get(i), expectedMethods.get(i)) == null) {
                missing.add(expectedActions.get(i) + " " + expectedMethods.get(i));
            }
        }

        for (Action action : mappings.getActions()) {
            if (!expectedActions.contains(action.name)) {
                extra.add(action.name + " " + action.getMethods());
            }
        }

        if (!missing.isEmpty()) {
            System.out.println("Controller is missing methods : " + missing);
        }

        if (!extra.isEmpty()) {
            System.out.println("Controller has extra methods : " + extra);
        }

        assert missing.isEmpty() && extra.isEmpty() : "Wrong number of methods. See above logs.";

        assert mappings.getActions().size() == 14 :
                "The size was " + mappings.getActions().size() + " instead of 14.";

    }

    @Test
    public void testParameterParsing() {
        DotNetControllerMappings mappings = getMvcStandardControllerMappings("InstructorController.cs");

        Action targetAction = mappings.getActionForNameAndMethod("Edit", "GET");

        assert targetAction != null : "Edit action was null. Can't continue.";

        assert targetAction.parameters.keySet().contains("id") : "Parameters didn't contain id.";
    }

    //  Keep this left out for now since Bind(Include="...") now requires the proper model definition
    //  and won't directly use those values as parameters; these test files don't have the appropriate
    //  model definitions
//    @Test
//    public void testBindIncludeSettings() {
//        DotNetControllerMappings mappings = getMvcStandardControllerMappings("BindingController.cs");
//
//        Action targetAction = mappings.getActionForNameAndMethod("Edit", "POST");
//
//        assert targetAction != null : "Edit action was null. Can't continue.";
//
//        assert targetAction.parameters.keySet().contains("ID") : "Parameters didn't contain id.";
//    }

    @Test
    public void testDefaultValuesParsing() {
        DotNetControllerMappings mappings = getMvcStandardControllerMappings("DefaultParametersController.cs");

        assert mappings.getActions().size() == 1 :
                "Had " + mappings.getActions().size() + " actions, should have had 1.";

        Set<String> parameters = mappings.getActions().get(0).parameters.keySet();

        assert parameters.contains("id") :
                "Parameters didn't contain id but should have: " + parameters;
        assert parameters.contains("type") :
                "Parameters didn't contain type but should have: " + parameters;
        assert parameters.contains("expires") :
                "Parameters didn't contain expires but should have: " + parameters;

        assert !parameters.contains("null") :
                "Parameters contained null but shouldn't have: " + parameters;
        assert parameters.size() == 3 :
                "Size should have been 3 but was " + parameters;
    }
}
