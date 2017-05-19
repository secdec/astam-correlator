package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Created by csotomayor on 5/12/2017.
 */
public class DjangoRouteParser implements EventBasedTokenizer{

    public static final SanitizedLogger LOG = new SanitizedLogger(DjangoRouteParser.class);

    private List<DjangoEndpoint> endpoints = list();

    //TODO add final strings for constants in url file imports
    private static final String
        URL_PATTERNS = "urlpatterns",
        URL = "url";

    private Phase phase = Phase.IMPORT;
    private ImportState importState = ImportState.START;
    private UrlPatternState urlPatternState = UrlPatternState.START;
    private UrlState urlState = UrlState.START;

    private enum Phase {
        IMPORT, URLPATTERN, URL
    }

    private enum ImportState {
        START, SOMETHING
    }

    private enum UrlPatternState {
        START, BRACKET
    }

    private enum UrlState {
        START, REGEX, VIEWPATH
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    //each url file can reference other url files to be parsed, call recursively
    //urls are found in urlpatterns = [..] section with reference to view (controller)
    @Override
    public void processToken(int type, int lineNumber, String stringValue) {


    }


}
