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
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.util.EndpointValidationStatistics;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

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

        for (File solutionFolder : findSolutionFolders(rootDirectory)) {
            generateMappings(rootDirectory, solutionFolder);
        }
        EndpointValidationStatistics.printValidationStats(generateEndpoints());
    }

    private void generateMappings(File rootDirectory, File solutionDirectory) {

        List<ViewModelParser> modelParsers = list();
        List<DotNetControllerMappings> controllerMappingsList = list();

        DotNetRouteMappings routeMappings = new DotNetRouteMappings();
        Collection<File> cSharpFiles = FileUtils.listFiles(solutionDirectory, new String[] { "cs" }, true);

        for (File file : cSharpFiles) {
            if (file != null && file.exists() && file.isFile() &&
                    file.getAbsolutePath().contains(solutionDirectory.getAbsolutePath())) {

                DotNetControllerParser endpointParser = new DotNetControllerParser(file);
                DotNetRoutesParser routesParser = new DotNetRoutesParser();
                ViewModelParser modelParser = new ViewModelParser();
                EventBasedTokenizerRunner.run(file, endpointParser, routesParser, modelParser);

                if (routesParser.hasValidMappings()) {
                    //assert routeMappings == null; // if the project has 2 routes files we want to know about it
                    routeMappings.importFrom(routesParser.mappings);
                }

                if (endpointParser.hasValidControllerMappings()) {
                    controllerMappingsList.addAll(endpointParser.mappings);
                }

                modelParsers.add(modelParser);
            }
        }

        DotNetModelMappings modelMappings = new DotNetModelMappings(modelParsers);

        generators.add(new DotNetEndpointGenerator(rootDirectory, routeMappings, modelMappings, controllerMappingsList));
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

    private List<File> findSolutionFolders(File rootDirectory) {
        Collection<File> slnFiles = FileUtils.listFiles(rootDirectory, new String[] { "sln" }, true);
        List<File> solutionFolders = list();
        for (File slnFile : slnFiles) {
            File parent = slnFile.getParentFile();
            if (!solutionFolders.contains(parent))
                solutionFolders.add(parent);
        }

        if (solutionFolders.isEmpty()) {
            solutionFolders.add(rootDirectory);
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
