package com.denimgroup.threadfix.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/12/2017.
 */
public interface AstamApplicationService {
    void writeApplicationToOutput(int applicationId, OutputStream outputStream) throws IOException;
}
