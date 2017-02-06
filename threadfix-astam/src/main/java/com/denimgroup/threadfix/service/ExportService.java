package com.denimgroup.threadfix.service;

import java.io.File;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface ExportService {
    File getAllZipFile();
    File getApplicationInformation(int applicationId);
    File getFindings(int applicationId);
    File getAttackSurface(int applicationId);
}
