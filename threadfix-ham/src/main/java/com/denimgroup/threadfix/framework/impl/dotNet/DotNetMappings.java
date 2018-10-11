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
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspCoreActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspStandardApiActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators.AspStandardMvcActionGenerator;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.CSharpFileParser;
import com.denimgroup.threadfix.framework.util.*;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.cleanTypeName;

/**
 * Created by mac on 6/16/14.
 */
public class DotNetMappings implements EndpointGenerator {

    final File             rootDirectory;

    List<DotNetEndpointGenerator> generators = list();

    @SuppressWarnings("unchecked")
    public DotNetMappings(@Nonnull File rootDirectory) {
        assert rootDirectory.exists() : "Root file did not exist.";
        assert rootDirectory.isDirectory() : "Root file was not a directory.";

        this.rootDirectory = rootDirectory;

        CachedDirectory cachedRootDirectory = new CachedDirectory(rootDirectory);

        for (File solutionFolder : findSolutionFolders(cachedRootDirectory)) {
            generateMappings(rootDirectory, solutionFolder);
        }
        EndpointValidationStatistics.printValidationStats(generateEndpoints());
    }

    private void generateMappings(File rootDirectory, File solutionDirectory) {

        CachedDirectory cachedSolutionDirectory = new CachedDirectory(solutionDirectory);

        boolean isDotNetCore = false;
        DotNetCoreDetector dotNetCoreDetector = new DotNetCoreDetector();
        for (File project : cachedSolutionDirectory.findFiles("*.csproj")) {
            EventBasedTokenizerRunner.run(project, dotNetCoreDetector);
            if (!dotNetCoreDetector.shouldContinue()) {
                isDotNetCore = dotNetCoreDetector.isAspDotNetCore();
                break;
            }
        }

        List<ViewModelParser> modelParsers = list();
        List<DotNetControllerMappings> controllerMappingsList = list();
        List<CSharpClass> classes = list();

        DotNetRouteMappings routeMappings = new DotNetRouteMappings();
        Collection<File> cSharpFiles = cachedSolutionDirectory.findFiles("*.cs");
        Map<String, RouteParameterMap> routeParameters = new HashMap<String, RouteParameterMap>();

        for (File file : cSharpFiles) {
            if (file != null && file.exists() && file.isFile() &&
                    file.getAbsolutePath().contains(solutionDirectory.getAbsolutePath())) {

                //DotNetControllerParser endpointParser = new DotNetControllerParser(file);
                DotNetParameterParser parameterParser = new DotNetParameterParser();
                DotNetRoutesParser routesParser = new DotNetRoutesParser();
                ViewModelParser modelParser = new ViewModelParser();
                // EventBasedTokenizerRunner.run(file, endpointParser, routesParser, modelParser, parameterParser);
                EventBasedTokenizerRunner.run(file, routesParser, modelParser, parameterParser);

                List<CSharpClass> parsedClasses = CSharpFileParser.parse(file);
                classes.addAll(parsedClasses);

                if (routesParser.hasValidMappings()) {
                    //assert routeMappings == null; // if the project has 2 routes files we want to know about it
                    routeMappings.importFrom(routesParser.mappings);
                }

//                if (endpointParser.hasValidControllerMappings()) {
//                    controllerMappingsList.addAll(endpointParser.mappings);
//                }

                if (!parameterParser.getParsedParameterReferences().isEmpty()) {
                    routeParameters.put(file.getAbsolutePath(), parameterParser.getParsedParameterReferences());
                }
                modelParsers.add(modelParser);
            }
        }

        expandBaseTypes(classes);

        List<AspActionGenerator> actionGenerators = list();
        if (isDotNetCore) {
            actionGenerators.add(new AspCoreActionGenerator(classes, routeParameters));
        } else {
            actionGenerators.add(new AspStandardApiActionGenerator(classes, routeParameters));
            actionGenerators.add(new AspStandardMvcActionGenerator(classes, routeParameters));
        }

        for (AspActionGenerator generator : actionGenerators) {
            controllerMappingsList.addAll(generator.generate());
        }

        Map<String, CSharpClass> classesByFile = map();
        for (CSharpClass cSharpClass : classes) {
            String filePath = cSharpClass.getFilePath();
            if (!classesByFile.containsKey(filePath)) {
                classesByFile.put(filePath, cSharpClass);
            } else {
                CSharpClass existingClass = classesByFile.get(filePath);
                if (cSharpClass.getName().endsWith("Controller") && !existingClass.getName().endsWith("Controller")) {
                    classesByFile.put(filePath, cSharpClass);
                }
            }
        }

        for (DotNetControllerMappings controllerMappings : controllerMappingsList) {
            CSharpClass controllerClass = classesByFile.get(controllerMappings.getFilePath());
            if (controllerClass != null) {
                controllerMappings.setControllerClass(controllerClass);
            }
        }

        DotNetModelMappings modelMappings = new DotNetModelMappings(modelParsers);

        generators.add(new DotNetEndpointGenerator(rootDirectory, routeMappings, modelMappings, classes, controllerMappingsList));
    }



    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        assert !generators.isEmpty();

        List<Endpoint> result = list();
        for (EndpointGenerator generator : generators) {
            result.addAll(generator.generateEndpoints());
        }

        return result;
    }

    @Override
    public Iterator<Endpoint> iterator() {
        assert !generators.isEmpty();

        return new MultiGeneratorIterator(generators);
    }

    private void expandBaseTypes(List<CSharpClass> classes) {
        Map<String, CSharpClass> namedClasses = map();
        for (CSharpClass csClass : classes) {
            namedClasses.put(cleanTypeName(csClass.getName()), csClass);
        }

        //  Flatten base types
        for (CSharpClass csClass : classes) {
            List<String> newBaseTypes = list();
            List<String> visitedBaseTypes = list();

            do {
                for (String baseType : newBaseTypes) {
                    if (!csClass.getBaseTypes().contains(baseType)) {
                        csClass.addBaseType(baseType);

                        CSharpClass baseTypeClass = namedClasses.get(cleanTypeName(baseType));
                        if (baseTypeClass != null) {
                            for (CSharpAttribute baseAttribute : baseTypeClass.getAttributes()) {
                                csClass.addAttribute(baseAttribute);
                            }
                        }
                    }
                }

                newBaseTypes.clear();

                for (String baseType : csClass.getBaseTypes()) {
                    String cleanedBaseType = cleanTypeName(baseType);
                    if (visitedBaseTypes.contains(cleanedBaseType)) {
                        continue;
                    }

                    if (namedClasses.containsKey(cleanedBaseType)) {
                        CSharpClass resolvedBaseType = namedClasses.get(cleanedBaseType);
                        newBaseTypes.addAll(resolvedBaseType.getBaseTypes());
                    }

                    visitedBaseTypes.add(cleanedBaseType);
                }
            } while (!newBaseTypes.isEmpty());
        }

        //  Insert inherited members
        for (CSharpClass csClass : classes) {
            for (String baseTypeName : csClass.getBaseTypes()) {
                CSharpClass baseType = namedClasses.get(cleanTypeName(baseTypeName));
                if (baseType == null) {
                    continue;
                }

                for (CSharpMethod method : baseType.getMethods()) {
                    if (!csClass.hasMethod(method)) {
                        csClass.addMethod(method);
                    }
                }
            }
        }
    }

    private List<File> findSolutionFolders(CachedDirectory rootDirectory) {
        Collection<File> slnFiles = rootDirectory.findFiles("*.sln");
        List<File> solutionFolders = list();
        for (File slnFile : slnFiles) {
            File parent = slnFile.getParentFile();
            if (!solutionFolders.contains(parent))
                solutionFolders.add(parent);
        }

        if (solutionFolders.isEmpty()) {
            solutionFolders.add(rootDirectory.getDirectory());
        }

        return FilePathUtils.findRootFolders(solutionFolders);
    }

    private class MultiGeneratorIterator implements Iterator<Endpoint> {

        private final Queue<Iterator> subIterators;
        private Iterator currentIterator;

        public MultiGeneratorIterator(List<DotNetEndpointGenerator> endpointIterators) {
            this.subIterators = new LinkedList<Iterator>();
            for (EndpointGenerator generator : endpointIterators) {
                this.subIterators.add(generator.iterator());
            }
            this.currentIterator = this.subIterators.remove();
        }

        @Override
        public boolean hasNext() {
            return this.currentIterator != null && this.currentIterator.hasNext();
        }

        @Override
        public Endpoint next() {
            Endpoint result = (Endpoint) this.currentIterator.next();

            if (!this.currentIterator.hasNext()) {
                this.currentIterator = this.subIterators.remove();
            }

            return result;
        }

        @Override
        public void remove() {
            this.next();
        }
    }
}
