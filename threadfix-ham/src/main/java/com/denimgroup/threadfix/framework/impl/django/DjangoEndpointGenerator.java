package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

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

    private File rootDirectory, rootUrlsFile;

    public DjangoEndpointGenerator(@Nonnull File rootDirectory) {
        assert rootDirectory.exists() : "Root file did not exist.";
        assert rootDirectory.isDirectory() : "Root file was not a directory.";

        this.rootDirectory = rootDirectory;

        findRootUrlsFile();

        generateMappings();
    }

    private void findRootUrlsFile() {
        File manageFile = new File(rootDirectory, "manage.py");
        assert manageFile.exists() : "manage.py does not exist in root directory";
        SettingsFinder settingsFinder = new SettingsFinder();
        EventBasedTokenizerRunner.run(manageFile, settingsFinder);

        File settingsFile = settingsFinder.getSettings(rootDirectory.getPath());
        assert settingsFile.exists() : "Settings file not found";
        UrlFileFinder urlFileFinder = new UrlFileFinder();

        if (settingsFile.isDirectory()) {
            for (File file : settingsFile.listFiles()) {
                EventBasedTokenizerRunner.run(file, urlFileFinder);
                if (!urlFileFinder.shouldContinue()) break;
            }
        } else {
            EventBasedTokenizerRunner.run(settingsFile, urlFileFinder);
        }
        assert !urlFileFinder.getUrlFile().isEmpty() : "Root URL file setting does not exist.";

        rootUrlsFile = new File(rootDirectory, urlFileFinder.getUrlFile());
    }

    private void generateMappings() {
        DjangoRouteParser routeParser = new DjangoRouteParser();
        EventBasedTokenizerRunner.run(rootUrlsFile, routeParser);


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

    static class SettingsFinder implements EventBasedTokenizer {

        String settingsLocation = "";
        boolean shouldContinue = true, foundSettingsLocation = false;

        public File getSettings(String rootDirectory) { return DjangoPathCleaner.buildPath(rootDirectory, settingsLocation); }

        @Override
        public boolean shouldContinue() {
            return shouldContinue;
        }

        @Override
        public void processToken(int type, int lineNumber, String stringValue) {
            if (type == StreamTokenizer.TT_WORD && stringValue.equals("DJANGO_SETTINGS_MODULE")) {
                foundSettingsLocation = true;
            } else if (foundSettingsLocation && type == StreamTokenizer.TT_WORD) {
                settingsLocation = DjangoPathCleaner.cleanStringFromCode(stringValue);
            }

            if (!settingsLocation.isEmpty()) {
                shouldContinue = false;
            }
        }
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
            } else if (foundURLSetting && type == StreamTokenizer.TT_WORD) {
                urlFile = DjangoPathCleaner.cleanStringFromCode(stringValue);
            }

            if (!urlFile.isEmpty()) {
                shouldContinue = false;
            }
        }
    }
}
