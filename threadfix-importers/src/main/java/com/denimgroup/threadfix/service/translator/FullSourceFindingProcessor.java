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
package com.denimgroup.threadfix.service.translator;

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.ProjectConfig;
import com.denimgroup.threadfix.framework.engine.ThreadFixInterface;
import com.denimgroup.threadfix.framework.engine.cleaner.PathCleaner;
import com.denimgroup.threadfix.framework.engine.cleaner.PathCleanerFactory;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabase;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabaseFactory;
import com.denimgroup.threadfix.framework.engine.parameter.ParameterParser;
import com.denimgroup.threadfix.framework.engine.parameter.ParameterParserFactory;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.ApplicationVersionService;
import com.denimgroup.threadfix.service.WebAttackSurfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.denimgroup.threadfix.data.entities.AuthenticationRequired.UNKNOWN;

@Service
class FullSourceFindingProcessor implements FindingProcessor {

    private static final SanitizedLogger LOG = new SanitizedLogger(FullSourceFindingProcessor.class);

    @Nullable
    private final EndpointDatabase database;

    @Nullable
    private final ParameterParser parameterParser;

    @Nonnull
    private final FindingProcessor noSourceProcessor;

    @Autowired
    private WebAttackSurfaceService webAttackSurfaceService;

    @Autowired
    private ApplicationVersionService applicationVersionService;

    private int numberMissed = 0, total = 0, foundParameter;
    private long startTime = 0L;

    public FullSourceFindingProcessor(ProjectConfig config, Scan scan) {
        PathCleaner cleaner = PathCleanerFactory.getPathCleaner(
                config.getFrameworkType(), ThreadFixInterface.toPartialMappingList(scan));

        noSourceProcessor = new NoSourceFindingProcessor(cleaner);

        database = EndpointDatabaseFactory.getDatabase(config.getRootFile(),
                config.getFrameworkType(), cleaner);

        parameterParser = ParameterParserFactory.getParameterParser(config);

        startTime = System.currentTimeMillis();

        LOG.info("Initialized with EndpointDatabase = " + database);
        LOG.info("Initialized with PathCleaner = " + cleaner);
        LOG.info("Initialized with ParameterParser = " + parameterParser);
    }

    @Override
    public void process(@Nonnull Finding finding) {
        String parameter = null;
        Endpoint endpoint = null;
        total++;

        if (parameterParser != null) {
            if (finding.getSurfaceLocation() != null) {
                parameter = parameterParser.parse(ThreadFixInterface.toEndpointQuery(finding));
                foundParameter++;
                if (parameter != null) {
                    finding.getSurfaceLocation().setParameter(parameter);
                }
            }
        }

        if (database != null) {
            endpoint = database.findBestMatch(ThreadFixInterface.toEndpointQuery(finding));
        }

        if (endpoint != null) {
            finding.setCalculatedFilePath(endpoint.getFilePath());
            finding.setCalculatedUrlPath(endpoint.getUrlPath());

            finding.setFoundHAMEndpoint(true);
            createWebAttackSurface(finding, endpoint);

            if (parameter != null) {
                finding.setEntryPointLineNumber(endpoint.getLineNumberForParameter(parameter));
            } else {
                finding.setEntryPointLineNumber(endpoint.getStartingLineNumber());
            }

            finding.setRawPermissions(endpoint.getRequiredPermissions());
            if (finding.getAuthenticationRequired() == UNKNOWN) {
                finding.setAuthenticationRequired(endpoint.getAuthenticationRequired());
            }

        } else {

            numberMissed++;

            // let's try without the parameter in order to degrade gracefully
            noSourceProcessor.process(finding);
        }
    }

    private void createWebAttackSurface(Finding finding, Endpoint endpoint) {
        WebAttackSurface webAttackSurface = new WebAttackSurface();

        SurfaceLocation surfaceLocation = new SurfaceLocation();
        surfaceLocation.setUrl(finding.getSurfaceLocation().getUrl());
        surfaceLocation.setHttpMethod(finding.getSurfaceLocation().getHttpMethod());
        surfaceLocation.setParameter(finding.getSurfaceLocation().getParameter());
        webAttackSurface.setSurfaceLocation(surfaceLocation);

        DataFlowElement dataFlowElement = new DataFlowElement();
        dataFlowElement.setLineNumber(endpoint.getStartingLineNumber());
        dataFlowElement.setSourceFileName(endpoint.getFilePath());
        dataFlowElement.setFinding(finding);
        webAttackSurface.setDataFlowElement(dataFlowElement);

        Application app = finding.getScan().getApplication();
        webAttackSurface.setApplicationVersion(applicationVersionService.loadAppVersionByName(app.getName(), app.getId()));

        webAttackSurfaceService.storeWebAttackSurface(webAttackSurface);
    }

    @Override
    public void printStatistics() {
        LOG.info("Printing statistics for FullSourceFindingProcessor.");

        LOG.info("Successfully found endpoints for " + (total - numberMissed) +
                " out of " + total + " findings " +
                "(" + (100.0 * (total - numberMissed) / total) + "%).");
        LOG.info("Successfully found parameters for " + foundParameter +
                " out of " + total + " findings " +
                "(" + (100.0 * foundParameter / total) + "%)");
        LOG.info("Processing took " + (System.currentTimeMillis() - startTime) + " ms.");

        noSourceProcessor.printStatistics();
    }
}
