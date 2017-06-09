package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.engine.cleaner.DefaultPathCleaner;
import com.denimgroup.threadfix.framework.engine.partial.PartialMapping;

import java.io.File;
import java.util.List;

/**
 * Created by csotomayor on 5/15/2017.
 */
public class DjangoPathCleaner extends DefaultPathCleaner {

    public DjangoPathCleaner(List<PartialMapping> partialMappings) {
        super(partialMappings);
    }

    public static File buildPath(String root, String input) {
        StringBuilder builder = new StringBuilder(64);
        builder.append(root).append("/").append(input);
        File file = new File(builder.toString());
        return file;
    }

    public static String cleanStringFromCode(String input) {
        return input.replace('.', '/');
    }
}
