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

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapperFactory;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPlugin;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPluginDetector;
import com.denimgroup.threadfix.framework.util.ParameterMerger;
import com.denimgroup.threadfix.framework.util.java.EntityMappings;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if (parsedClass != null) {
                discoveredClasses.add(parsedClass);
            }
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

        File webXmlFile = StrutsWebXmlParser.findWebXml(rootDirectory);

        if (webXmlFile != null) {
            StrutsWebXmlParser webXmlParser = new StrutsWebXmlParser(webXmlFile);
            project.setWebPath(webXmlParser.getPrimaryWebContentPath());
            project.setWebInfPath(webXmlParser.getWebInfFolderPath());

            StrutsWebPackBuilder webPackBuilder = new StrutsWebPackBuilder();
            File webContentRoot = new File(webXmlParser.getPrimaryWebContentPath());
            StrutsWebPack primaryWebPack = webPackBuilder.generate(webContentRoot);
            for (String welcomeFile : webXmlParser.getWelcomeFiles()) {
                primaryWebPack.addWelcomeFile(welcomeFile);
            }
            project.addWebPack(primaryWebPack);
        } else {
            log.warn("Couldn't find web.xml file, won't generate JSP web-packs");
        }

        ActionMapperFactory mapperFactory = new ActionMapperFactory(configurationProperties);
        this.actionMapper = mapperFactory.detectMapper(project);

        for (StrutsPlugin plugin : project.getPlugins()) {
            plugin.apply(project);
        }

        generateMaps(project);

        // Assign parametric route parameters
        Pattern routeParameterPattern = Pattern.compile("\\{(\\w+)[^\\}]*\\}");
        for (Endpoint endpoint : endpoints) {

            Matcher routeParameterMatcher = routeParameterPattern.matcher(endpoint.getUrlPath());
            List<String> parameterNames = list();
            while (routeParameterMatcher.find()) {
                String name = routeParameterMatcher.group(1);
                parameterNames.add(name.toLowerCase());
            }

            for (RouteParameter param : endpoint.getParameters().values()) {
                String commonName = param.getName().toLowerCase();
                if (parameterNames.contains(commonName)) {
                    parameterNames.remove(commonName);
                    param.setParamType(RouteParameterType.PARAMETRIC_ENDPOINT);
                    break;
                }
            }

            // Params not found previously need to be added as new parameters, ie if a child endpoint inherits a
            // route parameter from its parent endpoint
            for (String remainingParamName : parameterNames) {
                RouteParameter newParam = new RouteParameter(remainingParamName);
                newParam.setParamType(RouteParameterType.PARAMETRIC_ENDPOINT);
                endpoint.getParameters().put(remainingParamName, newParam);
            }
        }

        ParameterMerger genericMerger = new ParameterMerger();
        genericMerger.setCaseSensitive(false);
        genericMerger.mergeParametersIn(endpoints);


        for (Endpoint endpoint : endpoints) {
            Collection<RouteParameter> params = endpoint.getParameters().values();
            for (RouteParameter param : params) {
                if (param.getParamType() == RouteParameterType.UNKNOWN) {
                    log.debug("Missing parameter datatype for " + param.getName());
                }
            }
        }

    }

    private void generateMaps(StrutsProject project) {

        StrutsPageParameterDetector parameterDetector = new StrutsPageParameterDetector();
        List<StrutsDetectedParameter> inferredParameters = list();

        Collection<File> webFiles = FileUtils.listFiles(new File(project.getRootDirectory()), new String[] { "html", "xhtml", "jsp"}, true);

        for (File file : webFiles) {
            inferredParameters.addAll(parameterDetector.parseStrutsFormsParameters(file));
        }

        endpoints = list();
        endpoints.addAll(actionMapper.generateEndpoints(project, project.getPackages(), ""));

        expandModelFieldParameters(endpoints, project.classes);

        for (StrutsDetectedParameter inferred : inferredParameters) {
            List<Endpoint> relevantEndpoints = findEndpointsForUrl(inferred.targetEndpoint, endpoints);
            for (Endpoint endpoint : relevantEndpoints) {
                if (!endpoint.getParameters().containsKey(inferred.paramName)) {
                    RouteParameter newParam = new RouteParameter(inferred.paramName);
                    newParam.setParamType(RouteParameterType.FORM_DATA);
                    endpoint.getParameters().put(inferred.paramName, newParam);
                }
            }
        }
    }

    private void expandModelFieldParameters(Collection<Endpoint> endpoints, Collection<StrutsClass> parsedClasses) {
        for (Endpoint endpoint : endpoints) {
            Collection<String> paramNames = new ArrayList<String>(endpoint.getParameters().keySet());
            for (String paramName : paramNames) {
                RouteParameter param = endpoint.getParameters().get(paramName);
                StrutsClass modelType = findClassByName(parsedClasses, cleanArrayName(param.getDataTypeSource()));
                if (modelType == null) {
                    continue;
                }

                List<RouteParameter> effectiveParameters = expandModelToParameters(modelType, parsedClasses, new Stack<StrutsClass>(), null);
                Map<String, RouteParameter> namedParameters = map();
                for (RouteParameter modelParam : effectiveParameters) {
                    namedParameters.put(modelParam.getName(), modelParam);
                }
                endpoint.getParameters().remove(paramName);
                endpoint.getParameters().putAll(namedParameters);
            }
        }
    }

    private List<RouteParameter> expandModelToParameters(StrutsClass modelType, Collection<StrutsClass> referenceClasses, @Nonnull Stack<StrutsClass> previousModels, String namePrefix) {

        if (namePrefix == null) {
            namePrefix = "";
        }

        if (previousModels.contains(modelType)) {
            return list();
        }

        boolean isTopLevel = previousModels.isEmpty();

        previousModels.push(modelType);

        List<RouteParameter> result = list();
        Set<ModelField> modelFields = modelType.getProperties();
        for (ModelField field : modelFields) {
            String dataType = field.getType();
            StrutsClass fieldModelType = findClassByName(referenceClasses, cleanArrayName(dataType));
            if (fieldModelType == null) {
                RouteParameter newParam = new RouteParameter(field.getParameterKey());
                newParam.setDataType(dataType);
                newParam.setParamType(RouteParameterType.FORM_DATA);
                result.add(newParam);
            } else {
                String subParamsPrefix;
                if (namePrefix.isEmpty()) {
                    subParamsPrefix = field.getParameterKey();
                } else {
                    subParamsPrefix = namePrefix + "." + field.getParameterKey();
                }
                List<RouteParameter> modelSubParameters = expandModelToParameters(fieldModelType, referenceClasses, previousModels, subParamsPrefix);
                result.addAll(modelSubParameters);
            }
        }

        previousModels.pop();

        return result;
    }

    private String cleanArrayName(String paramName) {
        paramName = StringUtils.replace(paramName, "[", "");
        paramName = StringUtils.replace(paramName, "]", "");
        return paramName;
    }

    private StrutsClass findClassByName(Collection<StrutsClass> classes, String name) {
        for (StrutsClass strutsClass : classes) {
            if (strutsClass.getName().equalsIgnoreCase(name)) {
                return strutsClass;
            }
        }
        return null;
    }

    private List<Endpoint> findEndpointsForUrl(String url, Collection<Endpoint> endpoints) {
        Endpoint mainEndpoint = null;
        int mainRelevance = -1000000;
        List<Endpoint> result = list();

        for (Endpoint endpoint : endpoints) {
            int relevance = endpoint.compareRelevance(url);
            if (relevance > mainRelevance) {
                mainEndpoint = endpoint;
                mainRelevance = relevance;
            }
        }

        if (mainEndpoint == null) {
            return result;
        }

        for (Endpoint endpoint : endpoints) {
            if (endpoint.getFilePath().equals(mainEndpoint.getFilePath())) {
                result.add(endpoint);
            }
        }

        return result;
    }

    private String replaceJspTags(String jspText) {
        return jspText;
    }

    private String replaceStrutsTemplateTags(String strutsTemplateText) {
        return strutsTemplateText;
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
