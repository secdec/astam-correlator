package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.framework.engine.ProjectDirectory;
import com.denimgroup.threadfix.framework.engine.framework.FrameworkChecker;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by csotomayor on 5/9/2017.
 */
public class DjangoFrameworkChecker extends FrameworkChecker{

    private static final SanitizedLogger LOG = new SanitizedLogger(DjangoFrameworkChecker.class);

    @Nonnull
    @Override
    public FrameworkType check(@Nonnull ProjectDirectory directory) {
        Collection files = FileUtils.listFiles(directory.getDirectory(),
                new FileExtensionFileFilter("py"), TrueFileFilter.INSTANCE);

        LOG.info("Got " + files.size() + " .py files from the directory.");

        return files.isEmpty() ? FrameworkType.NONE : FrameworkType.PYTHON;
    }
}
