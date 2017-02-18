package com.denimgroup.threadfix.mapper;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Appmgmt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/12/2017.
 */
public class AstamApplicationMapper {
    private Appmgmt.ApplicationRegistration applicationRegistration;

    private Appmgmt.ApplicationRegistration.VersionControlRepository createVersionControlRepository(Application app) {
        Appmgmt.ApplicationRegistration.VersionControlRepository.Builder repoBuilder =
                Appmgmt.ApplicationRegistration.VersionControlRepository.newBuilder();

        String repoUrl = app.getRepositoryUrl();
        if (repoUrl != null) {
            repoBuilder.setRepositoryUrl(ProtobufMessageUtils.createUrl(repoUrl))
                .setRepositoryType(Appmgmt.ApplicationRegistration.VersionControlRepository.RepositoryType.GIT);
        }

        String repoUser = app.getRepositoryUserName();
        if (repoUser != null) {
            repoBuilder.setUsername(repoUser);
        }

        String repoToken = app.getRepositoryPassword();
        if (repoToken != null) {
            repoBuilder.setToken(repoToken);
        }

        return repoBuilder.build();
    }

    public void setApplication(Application app) {
        Organization organization = app.getOrganization();
        Appmgmt.ApplicationRegistration.VersionControlRepository repo = createVersionControlRepository(app);

        Appmgmt.ApplicationRegistration.Builder appBuilder = Appmgmt.ApplicationRegistration.newBuilder()
                .setId(ProtobufMessageUtils.createUUIDFromInt(app.getId()))
                .setName(app.getName())
                .setRecordData(ProtobufMessageUtils.createRecordData(app))
                .setOrganization(organization.getName())
                .setRepository(repo);

        String url = app.getUrl();
        if (url != null) {
            appBuilder.setInfoUrl(ProtobufMessageUtils.createUrl(url));
        }
        applicationRegistration = appBuilder.build();
    }

    public void writeApplicationToOutput(OutputStream outputStream) throws IOException {
        applicationRegistration.writeTo(outputStream);
    }
}