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
package com.denimgroup.threadfix.framework;


import com.denimgroup.threadfix.framework.util.FilePathUtils;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TestConstants {
    private TestConstants(){}


    private static final String VARIABLE_NAME = "PROJECTS_ROOT",
                        testRoot = FilePathUtils.normalizePath(System.getProperty(VARIABLE_NAME));

    static {
        if (System.getProperty(VARIABLE_NAME) == null) {
            throw new IllegalStateException("You must define " + VARIABLE_NAME);
        }
    }

    // TODO move relevant files to the src/test/resources folder and use that
    public static final String
        ROLLER_FOLDER_NAME = "roller-roller_5.1.1",
        PETCLINIC_FOLDER_NAME = "spring-petclinic-master",
        WAVSEP_FOLDER_NAME = "wavsep",
        BODGEIT_FOLDER_NAME = "bodgeit",
        ROLLER_SOURCE_LOCATION = testRoot + ROLLER_FOLDER_NAME,
        RAILSGOAT_FOLDER_NAME = "railsgoat-master",
        SPRING_MVC_SHOWCASE_FOLDER_NAME = "spring-mvc-showcase-master",
        SPRING_MVC_SHOWCASE_LOCATION = testRoot + SPRING_MVC_SHOWCASE_FOLDER_NAME,
        PETCLINIC_SOURCE_LOCATION = testRoot + PETCLINIC_FOLDER_NAME,
        WAVSEP_SOURCE_LOCATION = testRoot + WAVSEP_FOLDER_NAME,
        BODGEIT_SOURCE_LOCATION = testRoot + BODGEIT_FOLDER_NAME,
        RAILSGOAT_SOURCE_LOCATION = testRoot + RAILSGOAT_FOLDER_NAME,
        BODGEIT_JSP_ROOT = BODGEIT_SOURCE_LOCATION + "/root",
        SPRING_MVC_SHOWCASE_WEB_XML = SPRING_MVC_SHOWCASE_LOCATION + "/src/main/webapp/WEB-INF/web.xml",
        WAVSEP_WEB_XML = WAVSEP_SOURCE_LOCATION + "/WebContent/WEB-INF/web.xml",
        BODGEIT_WEB_XML = BODGEIT_JSP_ROOT + "/WEB-INF/web.xml",
        WEB_FORMS_ROOT = testRoot + "ASP.NET",
        WEB_FORMS_CONTOSO = WEB_FORMS_ROOT + "/ASP.NET Web Forms Application Using Entity Framework 4.0 Database First",
        RISK_E_UTILITY = WEB_FORMS_ROOT + "/riskE",
        WEBGOAT_DOT_NET = WEB_FORMS_ROOT + "/webgoat.net",
        DOT_NET_ROOT = testRoot + "/ASP.NET MVC",
        DOT_NET_SAMPLE = DOT_NET_ROOT + "/ASP.NET MVC Application Using Entity Framework Code First",
        FAKE_FILE = "",
        SPRING_CONTROLLERS_PREFIX = "/src/main/java/org/springframework/samples/petclinic/",
        SPRING_CRASH_CONTROLLER = SPRING_CONTROLLERS_PREFIX + "system/CrashController.java",
        SPRING_OWNER_CONTROLLER = SPRING_CONTROLLERS_PREFIX + "owner/OwnerController.java",
        SPRING_PET_CONTROLLER   = SPRING_CONTROLLERS_PREFIX + "owner/PetController.java",
        SPRING_VET_CONTROLLER   = SPRING_CONTROLLERS_PREFIX + "vet/VetController.java",
        SPRING_VISIT_CONTROLLER = SPRING_CONTROLLERS_PREFIX + "owner/VisitController.java",
        SPRING_MODELS_PREFIX = "/src/main/java/org/springframework/samples/petclinic/",
        SPRING_OWNER_MODEL = "owner/Owner.java",
        SPRING_CONTROLLER_WITH_CLASS_REQUEST_MAPPING = "ControllerWithClassAnnotation.java.txt",

        THREADFIX_SOURCE_ROOT =
            new File("")
                .getAbsoluteFile()
                .getParentFile()
                .getAbsolutePath()
                + "/"
        ;

    public static String getFolderName(String name) {

        if (testRoot == null) {
            throw new IllegalStateException("System variable " + VARIABLE_NAME + " was null. Fix it.");
        }

        String folderName = testRoot + name;

        assertTrue("Folder " + folderName + " wasn't found on the filesystem. Fix your configuration.",
                new File(folderName).exists());

        return folderName;
    }

}
