package com.denimgroup.threadfix.service;

import java.io.IOException;
import java.io.OutputStream;

public interface AstamAttackSurfaceService {
    void writeAttackSurfaceToOutput(int applicationId, OutputStream outputStream) throws IOException;
}
