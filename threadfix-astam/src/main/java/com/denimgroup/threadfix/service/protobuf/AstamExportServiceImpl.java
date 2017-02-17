package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.denimgroup.threadfix.mapper.AstamFindingsMapper;
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
    private final static String PROTOBUF_APP_REG_FILENAME = "applicationRegistration";
    private final static String PROTOBUF_DAST_SET_FILENAME = "dastFindingSet";
    private final static String PROTOBUF_SAST_SET_FILENAME = "sastFindingSet";
    private final static String PROTOBUF_CORRELATED_SET_FILENAME = "correlatedFindingSet";
    private final static String PROTOBUF_TOOL_SET_FILENAME = "externalToolSet";
    private final static String PROTOBUF_ATTACK_SURFACE_FILENAME = "entryPointWebSet";

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
            String path = app.getName() + "/";
            int appId = app.getId();
            zipOutputStream.putNextEntry(new ZipEntry(path));

            writeApplicationToOutput(appId, path, zipOutputStream);
            writeFindingsToOutput(appId, path, zipOutputStream);
            writeAttackSurfaceToOutput(appId, path, zipOutputStream);
        }

        zipOutputStream.close();
    }

    private void addZipFileEntry(String filePath, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(filePath + PROTOBUF_APP_EXT));
    }

    private void writeApplicationToOutput(int applicationId, String path, ZipOutputStream zipOutputStream)
            throws IOException {
        addZipFileEntry(path + PROTOBUF_APP_REG_FILENAME, zipOutputStream);
        astamApplicationService.writeApplicationToOutput(applicationId, zipOutputStream);
    }

    private void writeFindingsToOutput(int applicationId, String path, ZipOutputStream zipOutputStream)
            throws IOException {
         AstamFindingsMapper astamMapper = new AstamFindingsMapper(applicationId);

        addZipFileEntry(path + PROTOBUF_DAST_SET_FILENAME, zipOutputStream);
        astamFindingsService.writeDastFindingsToOutput(astamMapper, zipOutputStream);

        addZipFileEntry(path + PROTOBUF_SAST_SET_FILENAME, zipOutputStream);
        astamFindingsService.writeSastFindingsToOutput(astamMapper, zipOutputStream);

        addZipFileEntry(path + PROTOBUF_CORRELATED_SET_FILENAME, zipOutputStream);
        astamFindingsService.writeCorrelatedFindingsToOutput(astamMapper, zipOutputStream);

        addZipFileEntry(path + PROTOBUF_TOOL_SET_FILENAME, zipOutputStream);
        astamFindingsService.writeExternalToolsToOutput(astamMapper, zipOutputStream);
    }

    private void writeAttackSurfaceToOutput(int applicationId, String path, ZipOutputStream zipOutputStream)
            throws IOException {
        AstamAttackSurfaceMapper astamMapper = new AstamAttackSurfaceMapper(applicationId);

        addZipFileEntry(path + PROTOBUF_ATTACK_SURFACE_FILENAME, zipOutputStream);
        astamAttackSurfaceService.writeAttackSurfaceToOutput(astamMapper, zipOutputStream);
    }
}