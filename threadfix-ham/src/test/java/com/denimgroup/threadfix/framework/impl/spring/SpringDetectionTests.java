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

package com.denimgroup.threadfix.framework.impl.spring;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.framework.TestConstants;
import com.denimgroup.threadfix.framework.engine.framework.FrameworkCalculator;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SpringDetectionTests {

    @Test
    public void petclinicTest() {
        testTypeDetection(TestConstants.PETCLINIC_SOURCE_LOCATION);
    }

    @Test
    public void testMvcAjaxConfig() {
        testTypeDetection(TestConstants.getFolderName("spring-mvc-ajax"));
    }

    @Test
    public void testMvcShowcaseConfig() {
        testTypeDetection(TestConstants.getFolderName("spring-mvc-showcase-master"));
    }

    @Test
    public void testMvcChatConfig() {
        testTypeDetection(TestConstants.getFolderName("spring-mvc-chat"));
    }

    public static final String[] ALL_SPRING_APPS = {
            "atmosphere-spring-mvc",
            "classifiedsMVC",
            "denarius",
            "dogphone-spring-mongo",
            "exhubs",
            "spring-mvc-ajax",
            "spring-mvc-chat",
            "spring-mvc-movies",
            "spring-mvc-showcase-master",
            "SpringUserAuthSample",
            "springmvc-todomvc",
            "WebCalculator",
    };

    @Test
    public void testTheOtherWebapps() {
        for (String app : ALL_SPRING_APPS) {
            testTypeDetection(TestConstants.getFolderName(app));
        }
    }

    void testTypeDetection(String location) {
        List<FrameworkType> types = FrameworkCalculator.getTypes(new File(location));
        assertTrue("Didn't find Spring in " + location + ". Got: " + types, types.contains(FrameworkType.SPRING_MVC));
    }

}
