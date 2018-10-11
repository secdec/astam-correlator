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

package com.denimgroup.threadfix.framework.engine.framework;

import static com.denimgroup.threadfix.framework.TestConstants.*;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import javax.annotation.Nullable;

import com.denimgroup.threadfix.framework.util.PathUtil;
import org.junit.Test;

public class WebXMLParserTests {

    @Nullable
    ServletMappings mvcShowcase = WebXMLParser.getServletMappings(new File(SPRING_MVC_SHOWCASE_WEB_XML),
            new CachedDirectory(new File(SPRING_MVC_SHOWCASE_LOCATION)));
    @Nullable
    ServletMappings wavsep = WebXMLParser.getServletMappings(new File(WAVSEP_WEB_XML),
            new CachedDirectory(new File(WAVSEP_SOURCE_LOCATION)));
    @Nullable
    ServletMappings bodgeIt = WebXMLParser.getServletMappings(new File(BODGEIT_WEB_XML),
            new CachedDirectory(new File(BODGEIT_SOURCE_LOCATION)));

    ////////////////////////////////////////////////////////////////
    ///////////////////////////// Tests ////////////////////////////
    ////////////////////////////////////////////////////////////////
    
    @Test
    public void testFindWebXML() {
        String[]
                sourceLocations = { SPRING_MVC_SHOWCASE_LOCATION, WAVSEP_SOURCE_LOCATION, BODGEIT_SOURCE_LOCATION },
                webXMLLocations = { SPRING_MVC_SHOWCASE_WEB_XML, WAVSEP_WEB_XML, BODGEIT_WEB_XML };

        for (int i = 0; i < sourceLocations.length; i++) {
            File projectDirectory = new File(sourceLocations[i]);
            assertTrue(projectDirectory.exists());

            File file = new CachedDirectory(projectDirectory).findWebXML();
            assertTrue("File was null, check that " + projectDirectory + " is a valid directory.", file != null);
            assertTrue(file.getName().equals("web.xml"));

            assertTrue(file.getAbsolutePath() + " wasn't " + webXMLLocations[i],
                PathUtil.isEqualInvariant(file.getAbsolutePath(), webXMLLocations[i]));
        }
    }
    
    // TODO improve these tests.
    @Test
    public void testWebXMLParsing() {
        assertTrue(mvcShowcase.getClassMappings().size() == 1);
        assertTrue(mvcShowcase.getServletMappings().size() == 1);

        assertTrue(wavsep.getClassMappings().size() == 0);
        assertTrue(wavsep.getServletMappings().size() == 0);

        assertTrue(bodgeIt.getClassMappings().size() == 0);
        assertTrue(bodgeIt.getServletMappings().size() == 1);
    }
    
    @Test
    public void testTypeGuessing() {
        assertTrue(mvcShowcase.guessApplicationType() == FrameworkType.SPRING_MVC);
        assertTrue(wavsep.guessApplicationType() == FrameworkType.JSP);
        assertTrue(bodgeIt.guessApplicationType() == FrameworkType.JSP);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullInput() {
        new CachedDirectory(null).findWebXML();
    }

    // This one is IllegalArgumentException because they wrote that into the SAXParser implementation
    @Test(expected=IllegalArgumentException.class)
    public void testNullInputWebXMLParser() {
        WebXMLParser.getServletMappings(null, null);
    }

    @Test
    public void testBadInput() {
        File doesntExist = new File("This/path/doesnt/exist");

        assertTrue(new CachedDirectory(doesntExist).findWebXML() == null);


    }
}
