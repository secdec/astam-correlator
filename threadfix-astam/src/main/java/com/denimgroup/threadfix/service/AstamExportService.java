package com.denimgroup.threadfix.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface AstamExportService {
    void writeAllToOutput(OutputStream outputStream) throws IOException;
}