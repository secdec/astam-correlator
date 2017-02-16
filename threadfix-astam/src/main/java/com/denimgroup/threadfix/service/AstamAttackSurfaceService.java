package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;

import java.io.IOException;
import java.io.OutputStream;

public interface AstamAttackSurfaceService {
    void writeAttackSurfaceToOutput(AstamAttackSurfaceMapper mapper, OutputStream outputStream)
            throws IOException;
}
