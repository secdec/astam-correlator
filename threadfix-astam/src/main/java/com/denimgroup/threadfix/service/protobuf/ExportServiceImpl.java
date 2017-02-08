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

    public File getAllZipFile() {
        // Add each export to zip file
        return null;
    }

    public File getApplicationInformation(int applicationId) {
        return null;
    }

    public File getFindings(int applicationId) {
        return findingsService.getFindings(applicationId);
    }

    public File getAttackSurface(int applicationId) {
        return null;
    }
}