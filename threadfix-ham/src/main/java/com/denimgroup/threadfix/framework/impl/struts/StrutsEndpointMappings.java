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
import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapperFactory;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPlugin;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPluginDetector;
import com.denimgroup.threadfix.framework.util.*;
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

    private List<StrutsPackage> strutsPackages;
    private List<Endpoint> endpoints;
    private ActionMapper actionMapper;

    private String[] acceptedWebFileTypes = new String[] {
            ".jsp",
            ".html",
            ".xhtml",

            //  These shouldn't be web files, but if they exist we should
            //  report them
            ".sh",
            ".exe",
            ".bin"
    };

    public StrutsEndpointMappings(@Nonnull File rootDirectory) {

        CachedDirectory cachedRootDirectory = new CachedDirectory(rootDirectory);

        StrutsProjectDetector detector = new StrutsProjectDetector();
        log.debug("Detecting project folders");
        Collection<String> projectFolders = detector.findProjectPaths(cachedRootDirectory);
        log.debug("Detected " + projectFolders.size() + " projects");

        Collection<File> javaFiles = cachedRootDirectory.findFiles("*.java");
        Collection<StrutsClass> discoveredClasses = list();
        for (File javaFile : javaFiles) {
            StrutsClass parsedClass = new StrutsClassParser(javaFile).getResultClass();
            if (parsedClass != null) {
                discoveredClasses.add(parsedClass);
            }
        }

        StrutsCodebase codebase = new StrutsCodebase();
        codebase.addClasses(discoveredClasses);
        expandClassBaseTypes(codebase);

        this.endpoints = list();


        for (String projectFolderPath : projectFolders) {

            log.debug("Processing project at: " + projectFolderPath);

            CachedDirectory cachedProjectFolder = new CachedDirectory(new File(projectFolderPath));

            List<File> strutsConfigFiles = list();
            List<File> strutsPropertiesFiles = list();

            StrutsConfigurationProperties configurationProperties;

            Collection configFiles = cachedProjectFolder.findFiles("*.xml", "*.properties");

            for (Object configFile : configFiles) {
                File file = (File) configFile;
                if (file.getName().equals(STRUTS_CONFIG_NAME) || (file.getName().contains("struts") && file.getName().endsWith("xml")))
                    strutsConfigFiles.add(file);
                if (file.getName().equals(STRUTS_PROPERTIES_NAME))
                    strutsPropertiesFiles.add(file);
            }

            //  In the case of Ant projects, properties may be contained in the project file
            if (strutsPropertiesFiles.size() == 0) {
                //  We'd prefer to have the proper "struts.properties" file; in absence of that, we'll
                //  take what we can get
                for (Object configFile : configFiles) {
                    File file = (File) configFile;
                    if (!file.getName().endsWith(".properties")) {
                        continue;
                    }

                    strutsPropertiesFiles.add(file);
                }
            }

            configurationProperties = new StrutsConfigurationProperties();
            for (File cfgFile : strutsConfigFiles)
                configurationProperties.loadFromStrutsXml(cfgFile);
            for (File propsFile : strutsPropertiesFiles)
                configurationProperties.loadFromStrutsProperties(propsFile);


            strutsPackages = list();
            for (File cfgFile : strutsConfigFiles) {
                StrutsXmlParser strutsXmlParser = new StrutsXmlParser(configFiles);
                strutsPackages.addAll(strutsXmlParser.parse(cfgFile));
            }

            StrutsProject project = new StrutsProject(projectFolderPath);
            project.setConfiguration(configurationProperties);

            //  Add default action mappers

            project.addPackages(strutsPackages);
            project.setCodebase(codebase);

            for (StrutsPackage strutsPackage : strutsPackages) {
                project.addActions(strutsPackage.getActions());
            }

            for (StrutsAction action : project.getActions()) {
                if (action.getActClass() == null) {
                    continue;
                }
                StrutsClass classForAction = codebase.findClassByName(action.getActClass());
                if (classForAction != null) {
                    action.setActClassLocation(classForAction.getSourceFile());
                }
            }

            StrutsPluginDetector pluginDetector = new StrutsPluginDetector();
            for (StrutsPlugin plugin : pluginDetector.detectPlugins(cachedProjectFolder)) {
                log.info("Detected struts plugin: " + plugin);
                project.addPlugin(plugin);
            }

            File webXmlFile = StrutsWebXmlParser.findWebXml(cachedProjectFolder);

            if (webXmlFile != null) {
                StrutsWebXmlParser webXmlParser = new StrutsWebXmlParser(webXmlFile);
                project.setWebPath(webXmlParser.getPrimaryWebContentPath());
                project.setWebInfPath(webXmlParser.getWebInfFolderPath());

                StrutsWebPackBuilder webPackBuilder = new StrutsWebPackBuilder();
                webPackBuilder.acceptFileType(acceptedWebFileTypes);

                File webContentRoot = new File(webXmlParser.getPrimaryWebContentPath());
                if (!webContentRoot.isDirectory()) {
                    log.warn("Found a web.xml but the content root did not exist!");
                } else {
                    StrutsWebPack primaryWebPack = webPackBuilder.generate(webContentRoot);
                    for (String welcomeFile : webXmlParser.getWelcomeFiles()) {
                        primaryWebPack.addWelcomeFile(welcomeFile);
                    }
                    project.addWebPack(primaryWebPack);
                }
            } else {
                log.warn("Couldn't find web.xml file, won't generate JSP web-packs");
            }

            ActionMapperFactory mapperFactory = new ActionMapperFactory(configurationProperties);
            this.actionMapper = mapperFactory.detectMapper(project);

            for (StrutsPlugin plugin : project.getPlugins()) {
                log.debug("Applying Struts plugin " + plugin.getClass().getSimpleName());
                plugin.apply(project);
            }

            generateMaps(project, this.endpoints, rootDirectory);
            resolveDuplicateEndpoints();
            addFileParameters(endpoints, project);
        }

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
        Map<Endpoint, Map<String, RouteParameter>> mergedParameters = genericMerger.mergeParametersIn(endpoints);
        for (Endpoint remappedEndpoint : mergedParameters.keySet()) {
            Map<String, RouteParameter> endpointParameters = mergedParameters.get(remappedEndpoint);
            remappedEndpoint.getParameters().putAll(endpointParameters);
        }

        autoGroupVariants(endpoints);
        EndpointUtil.rectifyVariantHierarchy(endpoints);

        for (Endpoint endpoint : endpoints) {
            Collection<RouteParameter> params = endpoint.getParameters().values();
            for (RouteParameter param : params) {
                if (param.getParamType() == RouteParameterType.UNKNOWN) {
                    log.debug("Missing parameter datatype for " + param.getName());
                }
            }
        }
    }

    private void autoGroupVariants(List<Endpoint> endpoints) {
        List<Endpoint> distinctEndpoints = list();
        List<Endpoint> variantEndpoints = list();
        for (Endpoint endpoint : endpoints) {
            StrutsEndpoint strutsEndpoint = (StrutsEndpoint)endpoint;
            StrutsEndpoint existingDistinctEndpoint = null;
            boolean isDistinct = true;
            for (Endpoint distinct : distinctEndpoints) {
                StrutsEndpoint distinctStrutsEndpoint = (StrutsEndpoint)distinct;
                if (strutsEndpoint.getStartingLineNumber() == distinctStrutsEndpoint.getStartingLineNumber() &&
                        strutsEndpoint.getEndingLineNumber() == distinctStrutsEndpoint.getEndingLineNumber() &&
                        strutsEndpoint.getHttpMethod().equals(distinctStrutsEndpoint.getHttpMethod()) &&
                        strutsEndpoint.getFilePath().equals(distinctStrutsEndpoint.getFilePath())) {

                    // Endpoints must both have display paths or neither have display paths
                    if ((strutsEndpoint.getDisplayFilePath() == null) != (distinctStrutsEndpoint.getDisplayFilePath() == null)) {
                        continue;
                    }

                    if (strutsEndpoint.getDisplayFilePath() != null && !strutsEndpoint.getDisplayFilePath().equals(distinctStrutsEndpoint.getDisplayFilePath())) {
                        continue;
                    }

                    existingDistinctEndpoint = distinctStrutsEndpoint;
                    isDistinct = false;
                    break;
                }
            }

            if (isDistinct) {
                distinctEndpoints.add(endpoint);
            } else {
                // The "best" distinct endpoint has the shortest URL path of all variants
                if (existingDistinctEndpoint.getUrlPath().length() > strutsEndpoint.getUrlPath().length()) {
                    // Replace the old distinct endpoint and move the variants to the new one

                    strutsEndpoint.addVariants(existingDistinctEndpoint.getVariants());
                    strutsEndpoint.addVariant(existingDistinctEndpoint);
                    existingDistinctEndpoint.clearVariants();

                    distinctEndpoints.add(strutsEndpoint);

                    distinctEndpoints.remove(existingDistinctEndpoint);
                    variantEndpoints.add(existingDistinctEndpoint);
                } else if (!existingDistinctEndpoint.getUrlPath().equalsIgnoreCase(strutsEndpoint.getUrlPath())) {
                    existingDistinctEndpoint.addVariant(strutsEndpoint);
                    variantEndpoints.add(strutsEndpoint);
                }
            }
        }

        endpoints.removeAll(variantEndpoints);
    }

    // Note: The "is absolute file" check is a bit wonky here since the current `getRelativePath` logic
    //       may return paths with `/` prefix, making those paths "absolute" on unix systems. This logic
    //       is used throughout threadfix-ham, so rather than changing that frequently-used bit of logic
    //       to omit `/` prefix we'll just extend the check
    private Boolean isDefinitelyAbsolute(String path, String potentialParent) {
        File file = new File(path);
        return file.isAbsolute() && file.exists() && !new File(PathUtil.combine(potentialParent, path)).exists();
    }

    private void generateMaps(StrutsProject project, Collection<Endpoint> endpoints, File rootDirectory) {

        StrutsPageParameterDetector parameterDetector = new StrutsPageParameterDetector();
        List<StrutsDetectedParameter> inferredParameters = list();

        Collection<File> webFiles = project.getCachedDirectory().findFiles("*.html", "*.xhtml", "*.jsp");

        for (File file : webFiles) {
            inferredParameters.addAll(parameterDetector.parseStrutsFormsParameters(file));
        }

        List<Endpoint> newEndpoints = list();
        newEndpoints.addAll(actionMapper.generateEndpoints(project, project.getPackages(), ""));

        addEndpointParametersFromClasses(project, newEndpoints);

        expandModelFieldParameters(newEndpoints, project.getCodebase());

        // Modify inferred parameters to point to the proper endpoint
        for (StrutsDetectedParameter param : inferredParameters) {

            if (param.targetEndpoint.startsWith("/")) {
                continue;
            }

            String sourceFile = param.sourceFile;
            StrutsEndpoint servingEndpoint = null;
            for (Endpoint endpoint : newEndpoints) {
                StrutsEndpoint strutsEndpoint = (StrutsEndpoint)endpoint;
                String resultFilePath = strutsEndpoint.getDisplayFilePath();
                String fullResultFilePath = PathUtil.combine(project.getWebPath(), resultFilePath);
                if (sourceFile.equalsIgnoreCase(resultFilePath) || sourceFile.equalsIgnoreCase(fullResultFilePath)) {
                    servingEndpoint = strutsEndpoint;
                    break;
                }
            }
            if (servingEndpoint != null) {
                String baseEndpoint = servingEndpoint.getUrlPath();
                if (baseEndpoint.contains("/")) {
                    baseEndpoint = baseEndpoint.substring(0, baseEndpoint.lastIndexOf('/'));
                }

                param.targetEndpoint = PathUtil.combine(baseEndpoint, param.targetEndpoint);
            }
        }

        // Add inferred parameters to endpoints
        for (StrutsDetectedParameter inferred : inferredParameters) {
            List<Endpoint> relevantEndpoints = findEndpointsForUrl(inferred.targetEndpoint, newEndpoints);

            //  Generate new endpoints if an HTTP request method was detected for an endpoint, but no version
            //  of that endpoint has that HTTP method
            Map<String, List<String>> currentHttpMethods = map();
            for (Endpoint endpoint : relevantEndpoints) {
                List<String> currentMethods = currentHttpMethods.get(endpoint.getUrlPath());
                if (currentMethods == null) {
                    currentHttpMethods.put(endpoint.getUrlPath(), currentMethods = list());
                }

                if (!currentMethods.contains(endpoint.getHttpMethod())) {
                    currentMethods.add(endpoint.getHttpMethod());
                }
            }

            for (String endpointUrl : currentHttpMethods.keySet()) {
                List<String> supportedMethods = currentHttpMethods.get(endpointUrl);
                String inferredQueryMethod = inferred.queryMethod.toUpperCase();
                if (!supportedMethods.contains(inferredQueryMethod)) {
                    supportedMethods.add(inferredQueryMethod);

                    Endpoint endpoint = null;
                    for (Endpoint e : newEndpoints) {
                        if (e.getUrlPath().equalsIgnoreCase(endpointUrl)) {
                            endpoint = e;
                            break;
                        }
                    }

                    if (endpoint == null) {
                        continue;
                    }

                    StrutsEndpoint baseEndpoint = (StrutsEndpoint)endpoint;
                    StrutsEndpoint newEndpoint = new StrutsEndpoint(endpoint.getFilePath(), endpoint.getUrlPath(), inferredQueryMethod, baseEndpoint.getParameters());
                    newEndpoint.setDisplayFilePath(baseEndpoint.getDisplayFilePath());
                    newEndpoint.setLineNumbers(baseEndpoint.getStartingLineNumber(), baseEndpoint.getEndingLineNumber());
                    relevantEndpoints.add(newEndpoint);
                    newEndpoints.add(newEndpoint);
                }
            }



            // Apply inferred parameters
            for (Endpoint endpoint : relevantEndpoints) {

                if (!endpoint.getHttpMethod().equalsIgnoreCase(inferred.queryMethod)) {
                    continue;
                }

                if (!endpoint.getParameters().containsKey(inferred.paramName)) {
                    RouteParameter newParam = new StrutsInferredRouteParameter(inferred.paramName);
                    newParam.setParamType(RouteParameterType.FORM_DATA);
                    endpoint.getParameters().put(inferred.paramName, newParam);
                } else {
                    // Replace the original entry with a StrutsInferredRouteParameter
                    RouteParameter newParam = new StrutsInferredRouteParameter(inferred.paramName);
                    RouteParameter originalParam = endpoint.getParameters().get(inferred.paramName);

                    newParam.setDataType(originalParam.getDataTypeSource());
                    newParam.setParamType(originalParam.getParamType());
                    newParam.setAcceptedValues(originalParam.getAcceptedValues());

                    if (newParam.getAcceptedValues() == null || newParam.getAcceptedValues().size() == 0) {
                        newParam.setAcceptedValues(inferred.allowedValues);
                    }
                }
            }
        }

        // Cull parameters by whether their symbol was referenced
        for (Endpoint endpoint : newEndpoints) {
            List<String> culledParameters = list();
            StrutsEndpoint strutsEndpoint = (StrutsEndpoint)endpoint;
            String fullPath = PathUtil.combine(project.getRootDirectory() , endpoint.getFilePath());

            for (RouteParameter param : strutsEndpoint.getParameters().values()) {
                if (param instanceof StrutsInferredRouteParameter) {
                    continue;
                }

                StrutsMethod sourceMethod = project.getCodebase().findMethodByCodeLines(fullPath, endpoint.getStartingLineNumber());
                if (sourceMethod == null) {
                    continue;
                }

                //  Collect methods that the endpoint may defer to (they may reference the given parameter)
                List<StrutsMethod> relevantMethods = list();
                for (StrutsMethod method : sourceMethod.getDeclaringClass().getMethods()) {
                    if (sourceMethod.hasSymbolReference(method.getName())) {
                        relevantMethods.add(method);
                    }
                }
                relevantMethods.add(sourceMethod);

                String[] paramNameParts = param.getName().split("\\.");
                boolean hasReference = false;

                for (String part : paramNameParts) {
                    for (StrutsMethod method : relevantMethods) {
                        if (method.hasSymbolReference(part)) {
                            hasReference = true;
                            break;
                        }
                    }
                    if (hasReference) {
                        break;
                    }
                }

                if (!hasReference) {
                    culledParameters.add(param.getName());
                }
            }

            for (String param : culledParameters) {
                endpoint.getParameters().remove(param);
            }
        }

        // Resolve parameter data types
        for (Endpoint endpoint : newEndpoints) {
            String fullFilePath = PathUtil.combine(project.getRootDirectory(), endpoint.getFilePath());
            StrutsClass classForEndpoint = project.getCodebase().findClassByFileLocation(fullFilePath);
            if (classForEndpoint == null) {
                continue;
            }

            for (RouteParameter param : endpoint.getParameters().values()) {
                String name = param.getName();
                String[] pathParts = name.split("\\.");

                ModelField currentField = null;
                StrutsClass currentModelClass = classForEndpoint;
                for (String part : pathParts) {
                    if (currentModelClass == null) {
                        currentField = null;
                        break;
                    }

                    currentField = currentModelClass.getFieldOrProperty(part);
                    if (currentField == null) {
                        break;
                    }

                    currentModelClass = project.getCodebase().findClassByName(currentField.getType());
                }

                if (currentField != null) {
                    param.setDataType(currentField.getType());
                }
            }
        }

        // Make file paths relative to primary root folder for sub-projects
        String projectRelativeFilePath = FilePathUtils.getRelativePath(project.getRootDirectory(), rootDirectory);
        for (Endpoint endpoint : newEndpoints) {
            StrutsEndpoint strutsEndpoint = (StrutsEndpoint)endpoint;

            String filePath = strutsEndpoint.getFilePath();

            // (wonky "is absolute file" check)
            if (isDefinitelyAbsolute(filePath, project.getRootDirectory())) {
                filePath = FilePathUtils.getRelativePath(filePath, project.getRootDirectory());
            }
            String fullFilePath = PathUtil.combine(projectRelativeFilePath, filePath, true);
            strutsEndpoint.setFilePath(fullFilePath);

            String displayFilePath = strutsEndpoint.getDisplayFilePath();
            if (displayFilePath != null) {
                String fullDisplayFilePath;

                if (isDefinitelyAbsolute(displayFilePath, rootDirectory.getAbsolutePath())) {
                    fullDisplayFilePath = FilePathUtils.getRelativePath(displayFilePath, FilePathUtils.normalizePath(rootDirectory.getAbsolutePath()));
                } else {
                    fullDisplayFilePath = PathUtil.combine(projectRelativeFilePath, displayFilePath, true);
                }

                strutsEndpoint.setDisplayFilePath(fullDisplayFilePath);
            }
        }


        endpoints.addAll(newEndpoints);
    }

    private void addFileParameters(Collection<Endpoint> endpoints, StrutsProject project) {
        for (Endpoint endpoint : endpoints) {

            RouteParameter existingFileParameter = null;

            for (RouteParameter param : endpoint.getParameters().values()) {
                String rawType = param.getDataTypeSource();
                if (rawType != null && (rawType.equalsIgnoreCase("File") || rawType.equalsIgnoreCase("FileUploadForm") || rawType.equalsIgnoreCase("FormFile"))) {
                    existingFileParameter = param;
                    break;
                }
            }


            if (existingFileParameter != null) {
                existingFileParameter.setParamType(RouteParameterType.FILES);
                continue;
            }

            StrutsMethod sourceMethod = project.getCodebase().findMethodByCodeLines(endpoint.getFilePath(), endpoint.getStartingLineNumber());
            if (sourceMethod == null) {
                continue;
            }

            if (sourceMethod.hasSymbolReference("FileUploadForm") || sourceMethod.hasSymbolReference("FormFile")) {
                RouteParameter newParameter = new StrutsInferredRouteParameter("[File]");
                newParameter.setDataType("String");
                newParameter.setParamType(RouteParameterType.FILES);
                endpoint.getParameters().put(newParameter.getName(), newParameter);
            }
        }
    }

    private void expandModelFieldParameters(Collection<Endpoint> endpoints, StrutsCodebase codebase) {
        for (Endpoint endpoint : endpoints) {
            Collection<String> paramNames = new ArrayList<String>(endpoint.getParameters().keySet());
            for (String paramName : paramNames) {
                RouteParameter param = endpoint.getParameters().get(paramName);
                StrutsClass modelType = codebase.findClassByName(cleanArrayName(param.getDataTypeSource()));
                if (modelType == null) {
                    continue;
                }

                List<RouteParameter> effectiveParameters = expandModelToParameters(modelType, codebase, new Stack<StrutsClass>(), paramName);
                Map<String, RouteParameter> namedParameters = map();
                for (RouteParameter modelParam : effectiveParameters) {
                    namedParameters.put(modelParam.getName(), modelParam);
                }
                endpoint.getParameters().remove(paramName);
                endpoint.getParameters().putAll(namedParameters);
            }
        }
    }

    private List<RouteParameter> expandModelToParameters(StrutsClass modelType, StrutsCodebase codebase, @Nonnull Stack<StrutsClass> previousModels, String namePrefix) {

        if (namePrefix == null) {
            namePrefix = "";
        }

        if (previousModels.contains(modelType)) {
            return list();
        }

        previousModels.push(modelType);

        List<RouteParameter> result = list();
        Set<ModelField> modelFields = modelType.getProperties();
        modelFields.addAll(modelType.getFields());
        for (ModelField field : modelFields) {
            String dataType = field.getType();
            StrutsClass fieldModelType = codebase.findClassByName(cleanArrayName(dataType));

            if (previousModels.contains(fieldModelType)) {
                continue;
            }

            if (fieldModelType == null) {
                String paramName = field.getParameterKey();
                if (!namePrefix.isEmpty()) {
                    paramName = namePrefix + "." + paramName;
                }
                RouteParameter newParam = new RouteParameter(paramName);
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
                List<RouteParameter> modelSubParameters = expandModelToParameters(fieldModelType, codebase, previousModels, subParamsPrefix);
                result.addAll(modelSubParameters);
            }
        }

        previousModels.pop();

        return result;
    }

    private static final List<String> STRUTS_FRAMEWORK_TYPES = list("ActionSupport", "ValidationAware", "ModelAware");

    //  Import super-base types from base types
    private void expandClassBaseTypes(StrutsCodebase codebase) {
        for (StrutsClass strutsClass : codebase.getClasses()) {
            List<String> checkedBaseTypes = list();
            Queue<String> pendingBaseTypes = new LinkedList<String>();
            pendingBaseTypes.addAll(strutsClass.getBaseTypes());
            while (!pendingBaseTypes.isEmpty()) {
                String baseType = pendingBaseTypes.remove();
                if (checkedBaseTypes.contains(baseType)) {
                    continue;
                }

                StrutsClass baseClass = codebase.findClassByName(baseType);
                if (baseClass != null) {
                    pendingBaseTypes.addAll(baseClass.getBaseTypes());
                    //  Copy array to avoid concurrent modification
                    for (String newBase : new ArrayList<String>(baseClass.getBaseTypes())) {
                        strutsClass.addBaseType(newBase);
                    }

                    //  Don't import any properties or fields from framework base types (but
                    //  still allow expansion of that type-tree)
                    if (!STRUTS_FRAMEWORK_TYPES.contains(baseClass.getCleanName())) {
                        strutsClass.addAllMethods(baseClass.getMethods());

                        for (ModelField mf : baseClass.getFields()) {
                            strutsClass.addField(mf);
                        }
                    }
                }

                checkedBaseTypes.add(baseType);
            }
        }
    }

    private void addEndpointParametersFromClasses(StrutsProject project, Collection<Endpoint> endpoints) {

        StrutsCodebase codebase = project.getCodebase();
        String rootDirectory = project.getRootDirectory();

        for (Endpoint endpoint : endpoints) {

            String endpointFile = endpoint.getFilePath();
            endpointFile = PathUtil.normalizeSeparator(PathUtil.combine(rootDirectory, endpointFile));

            StrutsClass actionClass = codebase.findClassByFileLocation(endpointFile);
            if (actionClass == null) {
                continue;
            }

            Map<String, RouteParameter> parameters = endpoint.getParameters();

            for (StrutsMethod method : actionClass.getMethods()) {
                if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
                    String parameterName = method.getName().substring(3);
                    parameterName = Character.toLowerCase(parameterName.charAt(0)) + parameterName.substring(1);
                    if (!parameters.containsKey(parameterName)) {
                        String parameterType = null;
                        if (!"void".equals(method.getReturnType())) {
                            parameterType = method.getReturnType();
                        } else if (!method.getParameters().isEmpty()) {
                            parameterType = method.getParameters().values().iterator().next();
                        }

                        if (parameterType != null) {
                            RouteParameter newParameter = RouteParameter.fromDataType(parameterName, parameterType);
                            //  Assume form data
                            newParameter.setParamType(RouteParameterType.FORM_DATA);
                            parameters.put(parameterName, newParameter);
                        }
                    }
                }
            }
        }
    }

    private String cleanArrayName(String paramName) {
        paramName = StringUtils.replace(paramName, "[", "");
        paramName = StringUtils.replace(paramName, "]", "");
        return paramName;
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

        url = CodeParseUtil.trim(url, "/");

        int numUrlPaths = StringUtils.countMatches(url, "/");

        for (Endpoint endpoint : endpoints) {
            String trimmedEndpoint = CodeParseUtil.trim(endpoint.getUrlPath(), "/");
            if (!trimmedEndpoint.startsWith(url)) {
                continue;
            }

            int numEndpointPaths = StringUtils.countMatches(trimmedEndpoint, "/");
            if (numEndpointPaths != numUrlPaths) {
                continue;
            }

            if (endpoint.getStartingLineNumber() != mainEndpoint.getStartingLineNumber()) {
                continue;
            }


            if (endpoint.getFilePath().equals(mainEndpoint.getFilePath())) {
                result.add(endpoint);
            }
        }

        return result;
    }

    private void resolveDuplicateEndpoints() {
        //  Resolve duplicates in order of priority:
        //  1. Actions with user-defined classes over raw JSP files
        //  2. Actions declared nearest the end (which overwrite previous actions)

        Map<String, List<Endpoint>> mappedEndpoints = new HashMap<String, List<Endpoint>>();

        //  Work with a reversed list to prioritize last declarations
        List<Endpoint> flattenedEndpoints = EndpointUtil.flattenWithVariants(endpoints);
        Collections.reverse(flattenedEndpoints);

        for (final Endpoint endpoint : flattenedEndpoints) {
            String path = endpoint.getUrlPath() + "[" + endpoint.getHttpMethod() + "]";
            if (!mappedEndpoints.containsKey(path)) {
                mappedEndpoints.put(path, new ArrayList<Endpoint>() {{
                    add(endpoint);
                }});
            } else {
                mappedEndpoints.get(path).add(endpoint);
            }
        }

        List<Endpoint> invalidatedDuplicates = new LinkedList<Endpoint>();
        for (List<Endpoint> boundEndpoints : mappedEndpoints.values()) {
            if (boundEndpoints.size() < 2) {
                continue;
            }

            Endpoint bestEndpoint = null;
            for (Endpoint option : boundEndpoints) {
                if (bestEndpoint == null) {
                    bestEndpoint = option;
                } else if (option.getFilePath().toLowerCase().endsWith(".java")) {
                    bestEndpoint = option;
                }
            }

            //  If it's handled by a Java file, keep it as the best option; otherwise, use the last (highest priority)
            //  option in the list
            if (!bestEndpoint.getFilePath().toLowerCase().endsWith(".java")) {
                bestEndpoint = boundEndpoints.get(boundEndpoints.size() - 1);
            }

            for (Endpoint option : boundEndpoints) {
                if (option != bestEndpoint) {
                    invalidatedDuplicates.add(option);
                }
            }
        }

        //  Remove duplicates
        endpoints.removeAll(invalidatedDuplicates);
        for (Endpoint remaining : endpoints) {
            //  Search for duplicates in endpoint variants
            StrutsEndpoint strutsEndpoint = (StrutsEndpoint)remaining;
            for (Endpoint invalidated : invalidatedDuplicates) {
                if (strutsEndpoint.getVariants().contains(invalidated)) {
                    strutsEndpoint.removeVariant(invalidated);
                }
            }
        }

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
