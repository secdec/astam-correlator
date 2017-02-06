package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.service.ExportService;
import com.denimgroup.threadfix.service.FindingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Created by jsemtner on 2/3/2017.
 */
@Service
public class ExportServiceImpl implements ExportService {
    private final FindingsService findingsService;

    @Autowired
    public ExportServiceImpl(FindingsService findingsService) {
        this.findingsService = findingsService;
    }

    @Override
    public File getAllZipFile() {
        // Add each export to zip file
        return null;
    }

    @Override
    public File getApplicationInformation(int applicationId) {
        return null;
    }

    @Override
    public File getFindings(int applicationId) {
        // Save findings to file
        findingsService.getFindings(applicationId);

        return null;
    }

    @Override
    public File getAttackSurface(int applicationId) {
        return null;
    }
}