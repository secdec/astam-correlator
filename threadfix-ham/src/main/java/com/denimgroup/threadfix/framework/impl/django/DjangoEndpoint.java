package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Created by csotomayor on 4/25/2017.
 */
public class DjangoEndpoint extends AbstractEndpoint {

    public DjangoEndpoint() {

    }

    @Nonnull
    @Override
    public Set<String> getParameters() {
        return null;
    }

    @Nonnull
    @Override
    public Set<String> getHttpMethods() {
        return null;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return null;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return null;
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
