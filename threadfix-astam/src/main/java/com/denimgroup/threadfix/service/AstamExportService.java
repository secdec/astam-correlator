package com.denimgroup.threadfix.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface AstamExportService {
    File getAllZipFile();
    File getApplicationInformation(int applicationId);
    void writeFindingsToOutput(int applicationId, OutputStream outputStream) throws IOException;
    File getAttackSurface(int applicationId);
}