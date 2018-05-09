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

package com.denimgroup.threadfix.cli.endpoints;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.framework.FrameworkCalculator;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabase;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabaseFactory;
import com.denimgroup.threadfix.framework.engine.full.EndpointSerialization;
import com.denimgroup.threadfix.framework.engine.full.TemporaryExtractionLocation;
import com.denimgroup.threadfix.framework.util.EndpointUtil;
import com.denimgroup.threadfix.framework.util.EndpointValidationStatistics;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.data.interfaces.Endpoint.PrintFormat.JSON;

public class EndpointMain {
    private static final String FRAMEWORK_COMMAND = "-framework=";
    enum Logging {
        ON, OFF
    }

    static String PRINTLN_SEPARATOR = StringUtils.repeat('-', 10);

    static Logging logging = Logging.OFF;
    static Endpoint.PrintFormat printFormat = Endpoint.PrintFormat.DYNAMIC;
    static FrameworkType framework = FrameworkType.DETECT;
    static boolean simplePrint = false;
    static String pathListFile = null;

    static int totalDetectedEndpoints = 0;
    static int totalDetectedParameters = 0;

    public static void main(String[] args) {
        if (checkArguments(args)) {
            resetLoggingConfiguration();
            int numProjectsWithEndpoints = 0;
            int numProjects = 0;

            if (pathListFile != null) {
                System.out.println("Loading path list file at '" + pathListFile + "'");
                List<String> fileContents;
                boolean isLongComment = false;

                try {
                    fileContents = FileUtils.readLines(new File(pathListFile));
                    List<EndpointJob> requestedTargets = list();
                    int lineNo = 1;
                    for (String line : fileContents) {
                        line = line.trim();
                        if (line.startsWith("#!")) {
                            isLongComment = true;
                        } else if (line.startsWith("!#")) {
                            isLongComment = false;
                        } else if (!line.startsWith("#") && !line.isEmpty() && !isLongComment) {

                            FrameworkType frameworkType = FrameworkType.DETECT;
                            File asFile;
                            if (line.contains(":") && !(new File(line)).exists()) {
                                String[] parts = StringUtils.split(line, ":", 2);
                                frameworkType = FrameworkType.getFrameworkType(parts[0].trim());
                                asFile = new File(parts[1].trim());

                                if (frameworkType == FrameworkType.NONE || frameworkType == FrameworkType.DETECT) {
                                    System.out.println("WARN: Couldn't parse framework type: '" + frameworkType + "', for '" + asFile.getName() + "' using DETECT");
                                    frameworkType = FrameworkType.DETECT;
                                }
                            } else {
                                asFile = new File(line);
                            }

                            if (!asFile.exists()) {
                                System.out.println("WARN - Unable to find input path '" + line + "' at line " + lineNo + " of " + pathListFile);
                            } else if (!asFile.isDirectory() && !isZipFile(asFile.getAbsolutePath())) {
                                System.out.println("WARN - Input path '" + line + "' is not a directory or ZIP, at line " + lineNo + " of " + pathListFile);
                            } else {
                                EndpointJob newJob = new EndpointJob();
                                newJob.frameworkType = frameworkType;
                                newJob.sourceCodePath = asFile;
                                requestedTargets.add(newJob);
                            }
                        }
                        ++lineNo;
                    }

                    numProjects = requestedTargets.size();

                    boolean isFirst = true;
                    FrameworkType baseType = framework;
                    for (EndpointJob job : requestedTargets) {
                        if (isFirst) {
                            System.out.println(PRINTLN_SEPARATOR);
                            isFirst = false;
                        }
                        System.out.println("Beginning endpoint detection for '" + job.sourceCodePath.getAbsolutePath() + "'");
                        framework = job.frameworkType;
                        System.out.println("Using framework=" + framework);
                        List<Endpoint> generatedEndpoints = listEndpoints(job.sourceCodePath);
                        framework = baseType;
                        System.out.println("Finished endpoint detection for '" + job.sourceCodePath.getAbsolutePath() + "'");
                        System.out.println(PRINTLN_SEPARATOR);

                        if (!generatedEndpoints.isEmpty()) {
                            ++numProjectsWithEndpoints;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to read path-list at " + pathListFile);
                    printError();
                }
            } else {
	        ++numProjects;

                if (!listEndpoints(new File(args[0])).isEmpty()) {
                    ++numProjectsWithEndpoints;
                }
            }

            System.out.println("-- DONE --");
            System.out.println("Generated " + totalDetectedEndpoints + " total endpoints");
            System.out.println("Generated " + totalDetectedParameters + " total parameters");
            System.out.println(numProjectsWithEndpoints + "/" + numProjects + " projects had endpoints generated");

            if (printFormat != JSON) {
                System.out.println("To enable logging include the -debug argument");
            }
        } else {
            printError();
        }
    }

    private static boolean isZipFile(String filePath) {
    	String ext = FilenameUtils.getExtension(filePath).toLowerCase();
    	return
	        ext.equals("zip") ||
	        ext.equals("war");
    }

    private static boolean checkArguments(String[] args) {
        if (args.length == 0) {
            return false;
        }

        File rootFile = new File(args[0]);

        if (rootFile.exists() && rootFile.isDirectory() || args[0].startsWith("-path-list-file")) {

            List<String> arguments = list(args);

            if (rootFile.exists()) {
                arguments.remove(0);
            }

            for (String arg : arguments) {
                if (arg.equals("-debug")) {
                    logging = Logging.ON;
                } else if (arg.equals("-lint")) {
                    printFormat = Endpoint.PrintFormat.LINT;
                } else if (arg.equals("-json")) {
                    printFormat = JSON;
                } else if (arg.contains(FRAMEWORK_COMMAND)) {
                    String frameworkName = arg.substring(arg.indexOf(
                            FRAMEWORK_COMMAND) + FRAMEWORK_COMMAND.length(), arg.length());
                    framework = FrameworkType.getFrameworkType(frameworkName);
                } else if (arg.equals("-simple")) {
                    simplePrint = true;
                } else if (arg.startsWith("-path-list-file=")) {
                    String[] parts = arg.split("=");
                    String path = parts[1];
                    if (path == null || path.isEmpty()) {
                        System.out.println("Invalid -path-list-file argument, value is empty");
                        continue;
                    }
                    if (path.startsWith("\"") || path.startsWith("'")) {
                        path = path.substring(1);
                    }
                    if (path.endsWith("\"") || path.endsWith("'")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    pathListFile = path;
                } else {
                    System.out.println("Received unsupported option " + arg + ", valid arguments are -lint, -debug, -json, -path-list-file, and -simple");
                    return false;
                }
            }

            return true;

        } else {
            System.out.println("Please enter a valid file path as the first parameter.");
        }

        return false;
    }

    static void printError() {
        System.out.println("The first argument should be a valid file path to scan. Other flags supported: -lint, -debug, -json, -path-list-file, -simple");
    }

    private static int printEndpointWithVariants(int i, int currentDepth, Endpoint endpoint) {

        int numPrinted = 1;

        StringBuilder line = new StringBuilder();

        line.append('[');
        line.append(i);
        line.append("] ");

        for (int s = 0; s < currentDepth * 2; s++) {
            line.append('-');
        }
        if (currentDepth > 0) {
            line.append(' ');
        }

        line.append(endpoint.getHttpMethod());
        line.append(": ");
        line.append(endpoint.getUrlPath());

        line.append(" (");
        line.append(endpoint.getVariants().size());
        line.append(" variants): PARAMETERS=");
            line.append(endpoint.getParameters());

        line.append("; FILE=");
        line.append(endpoint.getFilePath());

        line.append(" (lines '");
        line.append(endpoint.getStartingLineNumber());
        line.append("'-'");
        line.append(endpoint.getEndingLineNumber());
        line.append("')");

        System.out.println(line.toString());

        for (Endpoint variant : endpoint.getVariants()) {
            numPrinted += printEndpointWithVariants(i + numPrinted, currentDepth + 1, variant);
        }

        return numPrinted;
    }

    private static List<Endpoint> listEndpoints(File rootFile) {
        List<Endpoint> endpoints = list();

        File sourceRootFile = rootFile;
        TemporaryExtractionLocation zipExtractor = null;
        if (TemporaryExtractionLocation.isArchive(rootFile.getAbsolutePath())) {
            zipExtractor = new TemporaryExtractionLocation(rootFile.getAbsolutePath());
            zipExtractor.extract();

            sourceRootFile = zipExtractor.getOutputPath();
        }

        if (framework == FrameworkType.DETECT) {
            framework = FrameworkCalculator.getType(sourceRootFile);
        }

        EndpointDatabase database = EndpointDatabaseFactory.getDatabase(sourceRootFile, framework);

        if (database != null) {
            endpoints = database.generateEndpoints();
        }

        //Collections.sort(endpoints);

        int numPrimaryEndpoints = endpoints.size();
        int numEndpoints = EndpointUtil.flattenWithVariants(endpoints).size();

        totalDetectedEndpoints += numEndpoints;

        if (endpoints.isEmpty()) {
            System.out.println("No endpoints were found.");

        } else {
            System.out.println("Generated " + numPrimaryEndpoints +
                                " distinct endpoints with " +
                                (numEndpoints - numPrimaryEndpoints) +
                                " variants for a total of " + numEndpoints +
                                " endpoints");

            if (!simplePrint) {
                if (printFormat == JSON) {
                    Endpoint.Info[] infos = getEndpointInfo(endpoints);

                    try {
                        String s = new ObjectMapper().writeValueAsString(infos);
                        System.out.println(s);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    int i = 0;
                    for (Endpoint endpoint : endpoints) {
                        //System.out.println(endpoint.getCSVLine(printFormat));
                        i += printEndpointWithVariants(i, 0, endpoint);
                    }
                }
            }
        }

        if (EndpointValidation.validateSerialization(framework, sourceRootFile, endpoints)) {
            System.out.println("Successfully validated serialization for these endpoints");
        } else {
            System.out.println("Failed to validate serialization for at least one of these endpoints");
        }

        int numMissingStartLine = 0;
        int numMissingEndLine = 0;
        int numSameLineRange = 0;
        for (Endpoint endpoint : EndpointUtil.flattenWithVariants(endpoints)) {
            if (endpoint.getStartingLineNumber() < 0) {
                numMissingStartLine++;
            }
            if (endpoint.getEndingLineNumber() < 0) {
                numMissingEndLine++;
            }
            if (endpoint.getStartingLineNumber() >= 0 && endpoint.getStartingLineNumber() == endpoint.getEndingLineNumber()) {
                numSameLineRange++;
            }
        }

        System.out.println(numMissingStartLine + " endpoints were missing code start line");
        System.out.println(numMissingEndLine + " endpoints were missing code end line");
        System.out.println(numSameLineRange + " endpoints had the same code start and end line");

        List<RouteParameter> detectedParameters = list();
        for (Endpoint endpoint : endpoints) {
            detectedParameters.addAll(endpoint.getParameters().values());
        }

        totalDetectedParameters += detectedParameters.size();

        System.out.println("Generated " + detectedParameters.size() + " parameters");

        Map<RouteParameterType, Integer> typeOccurrences = map();
        int numHaveDataType = 0;
        int numHaveParamType = 0;
        int numHaveAcceptedValues = 0;
        for (RouteParameter param : detectedParameters) {
            if (param.getDataType() != null) {
                ++numHaveDataType;
            }
            if (param.getParamType() != RouteParameterType.UNKNOWN) {
                ++numHaveParamType;
            }
            if (param.getAcceptedValues() != null && param.getAcceptedValues().size() > 0) {
                ++numHaveAcceptedValues;
            }

            if (!typeOccurrences.containsKey(param.getParamType())) {
                typeOccurrences.put(param.getParamType(), 1);
            } else {
                int o = typeOccurrences.get(param.getParamType());
                typeOccurrences.put(param.getParamType(), o + 1);
            }
        }

        int numParams = detectedParameters.size();
        System.out.println("- " + numHaveDataType + "/" + numParams + " have their data type");
        System.out.println("- " + numHaveAcceptedValues + "/" + numParams + " have a list of accepted values");
        System.out.println("- " + numHaveParamType + "/" + numParams + " have their parameter type");
        for (RouteParameterType paramType : typeOccurrences.keySet()) {
            System.out.println("--- " + paramType.name() + ": " + typeOccurrences.get(paramType));
        }

        if (zipExtractor != null) {
            zipExtractor.release();
        }

        return endpoints;
    }

    private static Endpoint.Info[] getEndpointInfo(List<Endpoint> endpoints) {
        List<Endpoint> allEndpoints = EndpointUtil.flattenWithVariants(endpoints);
        Endpoint.Info[] endpointsInfos = new Endpoint.Info[allEndpoints.size()];

        for (int i = 0; i < allEndpoints.size(); i++) {
            endpointsInfos[i] = Endpoint.Info.fromEndpoint(allEndpoints.get(i));
        }

        return endpointsInfos;
    }

    private static void resetLoggingConfiguration() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        String pattern = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(pattern));

        if (logging == Logging.ON) {
            console.setThreshold(Level.DEBUG);
        } else {
            console.setThreshold(Level.ERROR);
        }

        console.activateOptions();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(console);
    }
}