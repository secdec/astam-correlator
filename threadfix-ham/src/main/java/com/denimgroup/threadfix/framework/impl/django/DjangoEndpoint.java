package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.setFrom;

/**
 * Created by csotomayor on 4/25/2017.
 */
public class DjangoEndpoint extends AbstractEndpoint {

    private String filePath;
    private String urlPath;

    private Set<String> httpMethods;
    private Set<String> parameters;

    public DjangoEndpoint(String filePath, String urlPath,
                          Collection<String> httpMethods, Collection<String> parameters) {
        this.filePath = filePath;
        this.urlPath = urlPath;
        if (httpMethods != null)
            this.httpMethods = setFrom(httpMethods);
        if (parameters != null)
            this.parameters = setFrom(parameters);
    }

    @Nonnull
    @Override
    public Set<String> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return 0;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return 0;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return false;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }
}
