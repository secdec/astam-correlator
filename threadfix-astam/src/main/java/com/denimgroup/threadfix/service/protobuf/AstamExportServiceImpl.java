package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.service.AstamApplicationService;
import com.denimgroup.threadfix.service.AstamAttackSurfaceService;
import com.denimgroup.threadfix.service.AstamExportService;
import com.denimgroup.threadfix.service.AstamFindingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
@Service
public class AstamExportServiceImpl implements AstamExportService {
    private final static String PROTOBUF_APP_EXT = ".ser";

    private final ApplicationDao applicationDao;
    private final AstamApplicationService astamApplicationService;
    private final AstamFindingsService astamFindingsService;
    private final AstamAttackSurfaceService astamAttackSurfaceService;

    @Autowired
    public AstamExportServiceImpl(ApplicationDao applicationDao, AstamApplicationService astamApplicationService,
                                  AstamFindingsService astamFindingsService, AstamAttackSurfaceService astamAttackSurfaceService) {
        this.applicationDao = applicationDao;
        this.astamApplicationService = astamApplicationService;
        this.astamFindingsService = astamFindingsService;
        this.astamAttackSurfaceService = astamAttackSurfaceService;
    }

    public void writeAllToOutput(OutputStream outputStream) throws IOException {
        List<Application> applicationList = applicationDao.retrieveAllActive();

        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        for (int i=0; i<applicationList.size(); i++) {
            Application app = applicationList.get(i);
            int appId = app.getId();
            zipOutputStream.putNextEntry(new ZipEntry(appId + PROTOBUF_APP_EXT));
            writeApplicationToOutput(appId, zipOutputStream);
            writeFindingsToOutput(appId, zipOutputStream);
            writeAttackSurfaceToOutput(appId, zipOutputStream);
        }

        zipOutputStream.close();
    }

    public void writeApplicationToOutput(int applicationId, OutputStream outputStream) throws IOException {
        astamApplicationService.writeApplicationToOutput(applicationId, outputStream);
    }

    public void writeFindingsToOutput(int applicationId, OutputStream outputStream) throws IOException {
        astamFindingsService.writeFindingsToOutput(applicationId, outputStream);
    }

    public void writeAttackSurfaceToOutput(int applicationId, OutputStream outputStream) throws IOException {
        astamAttackSurfaceService.writeAttackSurfaceToOutput(applicationId, outputStream);
    }
}