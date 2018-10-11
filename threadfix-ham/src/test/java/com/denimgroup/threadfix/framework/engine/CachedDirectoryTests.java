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
package com.denimgroup.threadfix.framework.engine;

import com.denimgroup.threadfix.framework.TestConstants;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CachedDirectoryTests {

    @Nonnull
    CachedDirectory
        springDirectory = new CachedDirectory(new File(TestConstants.PETCLINIC_SOURCE_LOCATION));

    @Test
    public void testMultipleMatchResolution() {
        String[][] tests = {
            { "/mysql/data.sql", "/src/main/resources/db/mysql/data.sql" },
            { "/hsqldb/data.sql", "/src/main/resources/db/hsqldb/data.sql" },
        };

        for (String[] test : tests) {
            File file = springDirectory.findBestFile(test[0]);

            String result = springDirectory.findCanonicalFilePath(file.getAbsolutePath());

            assertTrue("Found " + result + " results instead of " + test[1] + " for " + test[0],
                    test[1].equals(result));
        }
    }

    @Test
    public void testStarFilePaths() {
        Object[][] tests = {
                { "po*.xml", 1 },
                { "*Entity.java", 2 },
                { "owner*.html", 2},
                { "*Controller*", 11 },
                { "P*tCl*icAp*t*va", 1 }
            };

        for (Object[] test : tests) {
            List<File> results = springDirectory.findBestFiles((String) test[0]);
            int numResults = results.size();
            assertTrue("Found " + numResults + " results instead of " + test[1] + " for " + test[0],
                    numResults == ((Integer) test[1]));
        }
    }

    @Test
    public void testCanonicalRoot() {
        String[][] tests = {
                { "/User/test/scratch/some/directory/petclinic/src/main/resources/db/mysql/data.sql", "/src/main/resources/db/mysql/data.sql" },
                { "/User/test/scratch/some/directory/petclinic/pom.xml", "/pom.xml" },
                { "/User/test/scratch/some/directory/petclinic/src/main/resources/application.properties", "/src/main/resources/application.properties" },
        };

        String root = "/User/test/scratch/some/directory/";

        for (String[] test : tests) {
            String result = springDirectory.findCanonicalFilePath(test[0], root);
            assertTrue("Found " + result + " instead of " + test[1] + " for " + test[0],
                    test[1].equals(result));
        }
    }



}
