package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.service.AstamExportService;
import com.denimgroup.threadfix.service.AstamFindingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
@Service
public class AstamExportServiceImpl implements AstamExportService {
    private final AstamFindingsService astamFindingsService;

    @Autowired
    public AstamExportServiceImpl(AstamFindingsService astamFindingsService) {
        this.astamFindingsService = astamFindingsService;
    }

    public File getAllZipFile() {
        // Add each export to zip file
        return null;
    }

    public File getApplicationInformation(int applicationId) {
        return null;
    }

    public void writeFindingsToOutput(int applicationId, OutputStream outputStream) throws IOException {
        astamFindingsService.writeFindingsToOutput(applicationId, outputStream);
    }

    public File getAttackSurface(int applicationId) {
        return null;
    }
}