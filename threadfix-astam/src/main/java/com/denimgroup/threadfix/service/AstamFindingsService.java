package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.mapper.AstamFindingsMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface AstamFindingsService {
    void writeDastFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeSastFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeCorrelatedFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeExternalToolsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
}
