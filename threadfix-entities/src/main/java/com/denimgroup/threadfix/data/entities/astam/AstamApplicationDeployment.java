package com.denimgroup.threadfix.data.entities.astam;

import com.denimgroup.threadfix.data.entities.ApplicationVersion;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class AstamApplicationDeployment extends AstamAuditableEntity{

    @Column(length = 50, nullable = false)
    @JsonView(Object.class)
    String name;

    @Column(name = "astamDeploymentTypeId", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @JsonView(Object.class)
    AstamDeploymentType deploymentType;

    @Column(name = "applicationVersionId", nullable = false)
    @JsonView(Object.class)
    ApplicationVersion applicationVersion;

    @Column(name = "applicationEnvironmentId", nullable = false)
    @JsonView(Object.class)
    AstamApplicationEnvironment applicationEnvironment;


    public AstamDeploymentType getDeploymentType() {
        return deploymentType;
    }

    public void setDeploymentType(AstamDeploymentType deploymentType) {
        this.deploymentType = deploymentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public AstamApplicationEnvironment getApplicationEnvironment() {
        return applicationEnvironment;
    }

    public void setApplicationEnvironment(AstamApplicationEnvironment applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }
}
