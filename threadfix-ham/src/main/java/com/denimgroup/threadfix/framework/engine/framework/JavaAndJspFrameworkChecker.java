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

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.impl.spring.SpringJavaConfigurationChecker;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import javax.annotation.Nonnull;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JavaAndJspFrameworkChecker extends FrameworkChecker {

    private FrameworkType checkMappings(@Nonnull CachedDirectory directory) {
        File webXML = directory.findWebXML();
        if (webXML != null && webXML.exists()) {
            ServletMappings mappings = WebXMLParser.getServletMappings(webXML, directory);

            if (mappings != null) {
                return mappings.guessApplicationType();
            }
        }

        return FrameworkType.NONE;
    }

    private boolean checkStruts(@Nonnull CachedDirectory directory) {
        Collection<File> configFiles = FileUtils.listFiles(directory.getDirectory(), new String[]{"xml", "properties"}, true);
        return StrutsConfigurationChecker.check(configFiles);
    }

    private boolean checkSpringMvc(@Nonnull CachedDirectory directory) {
        Collection<File> xmlFiles = FileUtils.listFiles(directory.getDirectory(), new FileExtensionFileFilter("xml"), TrueFileFilter.INSTANCE);
        for (File file : xmlFiles) {
            if (SpringJavaConfigurationChecker.checkXmlFile(file)) {
                return true;
            }
        }

        Collection<File> javaFiles = FileUtils.listFiles(directory.getDirectory(),
                new FileExtensionFileFilter("java"), TrueFileFilter.INSTANCE);

        for (File file : javaFiles) {
            if (SpringJavaConfigurationChecker.checkJavaFile(file)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkJsp(@Nonnull CachedDirectory directory) {
        Collection<File> jspFiles = FileUtils.listFiles(directory.getDirectory(), new FileExtensionFileFilter("jsp"), TrueFileFilter.INSTANCE);
        return jspFiles.size() > 0;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public FrameworkType check(@Nonnull CachedDirectory directory) {

        if (checkStruts(directory)) {
            return FrameworkType.STRUTS;
        }


        // check for SPRING
        if (checkSpringMvc(directory)) {
            return FrameworkType.SPRING_MVC;
        }

        // check for JSP
        if (checkJsp(directory)) {
            return FrameworkType.JSP;
        }

        FrameworkType frameworkType = checkMappings(directory);
        if (frameworkType != FrameworkType.NONE) {
            return frameworkType;
        }

        return FrameworkType.NONE;
    }

    @Nonnull
    @Override
    public List<FrameworkType> checkForMany(@Nonnull CachedDirectory directory) {
        List<FrameworkType> frameworkTypes = list();

        FrameworkType frameworkType = checkMappings(directory);
        if (frameworkType != FrameworkType.NONE) {
            frameworkTypes.add(frameworkType);
        }

        if (checkStruts(directory) && !frameworkTypes.contains(FrameworkType.STRUTS)) {
            frameworkTypes.add(FrameworkType.STRUTS);
        }

        if (checkSpringMvc(directory) && !frameworkTypes.contains(FrameworkType.SPRING_MVC)) {
            frameworkTypes.add(FrameworkType.SPRING_MVC);
        }

        if (checkJsp(directory) && !frameworkTypes.contains(FrameworkType.JSP)) {
            frameworkTypes.add(FrameworkType.JSP);
        }

        return frameworkTypes;
    }
}
