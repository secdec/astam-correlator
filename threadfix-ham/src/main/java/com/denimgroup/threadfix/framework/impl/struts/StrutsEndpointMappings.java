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
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapperFactory;
import com.denimgroup.threadfix.framework.impl.struts.mappers.DefaultActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.mappers.PrefixBasedActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPlugin;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPluginDetector;
import com.denimgroup.threadfix.framework.util.java.EntityMappings;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class StrutsEndpointMappings implements EndpointGenerator {

    static final SanitizedLogger log = new SanitizedLogger(StrutsEndpointMappings.class.getName());

    private final String STRUTS_CONFIG_NAME = "struts.xml";
    private final String STRUTS_PROPERTIES_NAME = "struts.properties";

    private File rootDirectory;
    private Collection<File> javaFiles;
    private List<StrutsPackage> strutsPackages;
    private EntityMappings entityMappings;
    private List<Endpoint> endpoints;
    private StrutsConfigurationProperties configurationProperties;
    private ActionMapper actionMapper;

    public StrutsEndpointMappings(@Nonnull File rootDirectory) {
        this.rootDirectory = rootDirectory;
//        urlToControllerMethodsMap = map();
        List<File> strutsConfigFiles = list();
        File strutsPropertiesFile = null;

        entityMappings = new EntityMappings(rootDirectory);

        if (rootDirectory.exists()) {
            javaFiles = FileUtils.listFiles(rootDirectory,
                    new FileExtensionFileFilter("java"), TrueFileFilter.TRUE);
        } else {
            javaFiles = Collections.emptyList();
        }

        String[] configExtensions = {"xml", "properties"};
        Collection configFiles = FileUtils.listFiles(rootDirectory, configExtensions, true);

        for (Object configFile : configFiles) {
            File file = (File) configFile;
            if (file.getName().equals(STRUTS_CONFIG_NAME) || (file.getName().contains("struts") && file.getName().endsWith("xml")))
                strutsConfigFiles.add(file);
            if (file.getName().equals(STRUTS_PROPERTIES_NAME) && strutsPropertiesFile == null)
                strutsPropertiesFile = file;
        }

        Collection<File> javaFiles = FileUtils.listFiles(rootDirectory, new String[] { "java" }, true);
        Collection<StrutsClass> discoveredClasses = list();
        for (File javaFile : javaFiles) {
            StrutsClass parsedClass = new StrutsClassParser(javaFile).getResultClass();
            discoveredClasses.add(parsedClass);
        }

        configurationProperties = new StrutsConfigurationProperties();
        for (File cfgFile : strutsConfigFiles)
            configurationProperties.loadFromStrutsXml(cfgFile);
        if (strutsPropertiesFile != null)
            configurationProperties.loadFromStrutsProperties(strutsPropertiesFile);


        strutsPackages = list();
        for (File cfgFile : strutsConfigFiles) {
            StrutsXmlParser strutsXmlParser = new StrutsXmlParser(configFiles);
            strutsPackages.addAll(strutsXmlParser.parse(cfgFile));
        }

        StrutsProject project = new StrutsProject(rootDirectory.getAbsolutePath());
        project.setConfiguration(configurationProperties);

        //  Add default action mappers

        project.addPackages(strutsPackages);
        project.addClasses(discoveredClasses);

        for (StrutsPackage strutsPackage : strutsPackages) {
            project.addActions(strutsPackage.getActions());
        }

        for (StrutsAction action : project.getActions()) {
            if (action.getActClass() == null) {
                continue;
            }
            StrutsClass classForAction = project.findClassByName(action.getActClass());
            if (classForAction != null) {
                action.setActClassLocation(classForAction.getSourceFile());
            }
        }

        StrutsPluginDetector pluginDetector = new StrutsPluginDetector();
        for (StrutsPlugin plugin : pluginDetector.detectPlugins(rootDirectory)) {
            log.info("Detected struts plugin: " + plugin);
            project.addPlugin(plugin);
        }

        StrutsWebXmlParser webXmlParser = new StrutsWebXmlParser(StrutsWebXmlParser.findWebXml(rootDirectory));
        project.setWebPath(webXmlParser.getPrimaryWebContentPath());
        project.setWebInfPath(webXmlParser.getWebInfFolderPath());

        StrutsWebPackBuilder webPackBuilder = new StrutsWebPackBuilder();
        File webContentRoot = new File(webXmlParser.getPrimaryWebContentPath());
        StrutsWebPack primaryWebPack = webPackBuilder.generate(webContentRoot);
        for (String welcomeFile : webXmlParser.getWelcomeFiles()) {
            primaryWebPack.addWelcomeFile(welcomeFile);
        }
        project.addWebPack(primaryWebPack);

        ActionMapperFactory mapperFactory = new ActionMapperFactory(configurationProperties);
        this.actionMapper = mapperFactory.detectMapper(project);

        for (StrutsPlugin plugin : project.getPlugins()) {
            plugin.apply(project);
        }

        generateMaps(project);

    }

    private void generateMaps(StrutsProject project) {
        endpoints = list();
        endpoints.addAll(actionMapper.generateEndpoints(project, project.getPackages(), ""));
    }


    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return endpoints;
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Endpoint> iterator() {
        return generateEndpoints().iterator();
    }
}
