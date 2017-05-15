package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.sun.xml.internal.fastinfoset.stax.events.EventBase;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by csotomayor on 4/27/2017.
 */
public class DjangoEndpointGenerator implements EndpointGenerator{

    private static final SanitizedLogger LOG = new SanitizedLogger(DjangoEndpointGenerator.class);

    private List<Endpoint> endpoints;

    private File rootDirectory;

    public DjangoEndpointGenerator(@Nonnull File rootDirectory) {
        assert rootDirectory.exists() : "Root file did not exist.";
        assert rootDirectory.isDirectory() : "Root file was not a directory.";

        File settingsFile = new File(rootDirectory, "/settings.py");
        if (!settingsFile.exists()) {
            LOG.error("File /config/routes.rb not found. Exiting.");
            return;
        }

        UrlFileFinder finder = new UrlFileFinder();
        EventBasedTokenizerRunner.run(settingsFile, finder);
        assert !finder.getUrlFile().isEmpty() : "Root URL file setting does not exist.";

        File routesFile = new File(rootDirectory, finder.getUrlFile());
        DjangoRouteParser routeParser = new DjangoRouteParser();
        EventBasedTokenizerRunner.run(routesFile, routeParser);


    }

    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return endpoints;
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return endpoints.iterator();
    }

    static class UrlFileFinder implements EventBasedTokenizer {

        String urlFile = "";
        boolean shouldContinue = true, foundURLSetting = false;

        public String getUrlFile() { return urlFile; }

        @Override
        public boolean shouldContinue() {
            return shouldContinue;
        }

        @Override
        public void processToken(int type, int lineNumber, String stringValue) {
            if (type == StreamTokenizer.TT_WORD && stringValue.equals("ROOT_URLCONF")) {
                foundURLSetting = true;
            } else if (foundURLSetting == true && type == StreamTokenizer.TT_WORD) {
                urlFile = DjangoPathCleaner.cleanStringFromCode(stringValue);
            }

            if (!urlFile.isEmpty()) {
                shouldContinue = false;
            }
        }
    }
}
