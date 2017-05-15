package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.engine.cleaner.PathCleaner;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by csotomayor on 5/15/2017.
 */
public class DjangoPathCleaner implements PathCleaner {

    public static String cleanStringFromCode(String input) {
        return "/" + input.replace('.', '/').concat(".py");
    }

    public static String cleanStringFromScan(String input) {
        return input;
    }

    @Nullable
    @Override
    public String cleanStaticPath(@Nonnull String filePath) {
        return filePath;
    }

    @Nullable
    @Override
    public String cleanDynamicPath(@Nonnull String urlPath) {
        return urlPath;
    }

    @Nullable
    @Override
    public String getDynamicPathFromStaticPath(@Nonnull String filePath) {
        return filePath;
    }

    @Nullable
    @Override
    public String getDynamicRoot() {
        return null;
    }

    @Nullable
    @Override
    public String getStaticRoot() {
        return null;
    }

    @Override
    public void setEndpointGenerator(EndpointGenerator generator) {

    }
}
