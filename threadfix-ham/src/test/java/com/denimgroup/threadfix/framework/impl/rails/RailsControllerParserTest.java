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
package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsController;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsControllerMethod;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.framework.TestConstants.RAILSGOAT_SOURCE_LOCATION;
import static org.junit.Assert.assertTrue;

/**
 * Created by sgerick on 4/27/2015.
 */
public class RailsControllerParserTest {

    private static final String[][] RAILSGOAT_CONTROLLERS = new String [][]{
            //  {"ctrl", "method1", "method2", "method3"},
            {"Admin", "dashboard","analytics","get_all_users","get_user","update_user","delete_user"},
            {"BenefitForms", "index","download","upload"},
            {"Users", "new","create","account_settings","update"},
            {"Messages", "index","show","destroy","create"},
    };
    private static final String[][] RAILSGOAT_USERS_CONTROLLER = new String [][]{
            //  {"method", "param1", "param2", "param3"},
            {"new"},
            {"create","user.email","user.admin","user.first_name","user.last_name","user.user_id","user.password",
                    "user.password_confirmation","user.skip_user_id_assign","user.skip_hash_password"},
            {"account_settings"},
            {"update", "user", "user.password", "user.password_confirmation"}
    };
    private static final String[][] RAILSGOAT_ADMIN_CONTROLLER = new String[][]{
        //  Note that all these also require the "admin_id" param, but this is
        //  set up in 'before_action' which is currently unsupported
        {"dashboard"},
        {"analytics", "field", "ip"},
        {"get_user", "admin_id"},
        {"update_user", "admin_id", "user.password"},
        {"delete_user", "admin_id"},
    };
    private static final String[][] RAILSGOAT_MESSAGES_CONTROLLER = new String [][]{
            //  {"method", "param1", "param2", "param3"},
            {"index"},
            {"show","id"},
            {"destroy","id"},
            {"create", "message.creator_id","message.message","message.read","message.receiver_id"}
    };

    private List<RailsController> railsControllers;


    @Test
    public void testRailsGoatControllerParser() {
        File f = new File(RAILSGOAT_SOURCE_LOCATION);
        assert(f.exists());
        assert(f.isDirectory());
        //System.err.println("parsing "+f.getAbsolutePath() );
        railsControllers = (List<RailsController>) RailsControllerParser.parse(f);
        //System.err.println(System.lineSeparator() + "Parse done." + System.lineSeparator());

        checkControllers(RAILSGOAT_CONTROLLERS);

        //  TODO - Re-enable these test cases
        // These Railsgoat controllers seem to use a different method of accessing params now than
        //  when this test was first written, using 'params_*' methods to validate and retrieve
        //  that object. The simplest workaround would probably be parsing the ERB template. Otherwise
        //  method calls would need to be followed to find that the given model type was being referenced
        //checkController("Users", RAILSGOAT_USERS_CONTROLLER);
        //checkController("Messages",RAILSGOAT_MESSAGES_CONTROLLER);

        checkController("Admin", RAILSGOAT_ADMIN_CONTROLLER);
    }

    private void checkControllers(String[][] testControllers) {
        for (String[] testCtrl : testControllers) {
            String testCtrlName = testCtrl[0];
            RailsController rc = getRailsControllerByName(testCtrlName, null);
            assertTrue("Controller not found: " + testCtrlName, (rc != null) );
            for (int i=1; i < testCtrl.length; i++) {
                String testCtrlMethodName = testCtrl[i];
                boolean found = false;
                for (RailsControllerMethod rcm : rc.getControllerMethods()) {
                    if (testCtrlMethodName.equalsIgnoreCase(rcm.getMethodName())) {
                        found = true;
                        break;
                    }
                }
                assertTrue("Controller.method not found: " + testCtrlName.concat(".").concat(testCtrlMethodName),
                        found);
            }
        }
    }

    private void checkController(String ctrlName, String[][] testController) {
        RailsController rc = getRailsControllerByName(ctrlName, null);
        assertTrue("Controller not found: " + ctrlName, (rc != null) );
        for (String[] testMethod : testController) {
            boolean methodFound = false;
            String testMethodName = testMethod[0];
            for (RailsControllerMethod rcm : rc.getControllerMethods()) {
                String methodName = rcm.getMethodName();
                if (testMethodName.equals(methodName)) {
                    methodFound = true;
                    for (int i=1; i < testMethod.length; i++) {
                        String testParam = testMethod[i];
                        assertTrue("Controller.method[:param] not found: "
                                    + ctrlName.concat(".").concat(testMethodName)
                                    + "[" + testParam +  "]",
                                rcm.getMethodParams().keySet().contains(testParam));
                    }
                    break;
                }
            }
            assertTrue("Controller.method not found: " + ctrlName.concat(".").concat(testMethodName),
                    methodFound);
        }
    }

    private RailsController getRailsControllerByName(String ctrlName, String moduleName) {
        RailsController rcReturn = null;
        for (RailsController rc : railsControllers) {
            if (ctrlName.equalsIgnoreCase(rc.getControllerName()) &&
                    ((moduleName == null && rc.getModuleName() == null) ||
                    ((moduleName != null && moduleName.equalsIgnoreCase(rc.getModuleName()))))
                    ) {
                rcReturn = rc;
                break;
            }
        }
        return rcReturn;
    }


}

