package com.denimgroup.threadfix.data.entities.astam;

import com.denimgroup.threadfix.data.entities.ApplicationVersion;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
public class AstamApplicationDeployment extends AstamAuditableEntity{

    @Column(length = 50, nullable = false)
    @JsonView(Object.class)
    String name;

    @Column(name = "astamDeploymentTypeId", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @JsonView(Object.class)
    AstamDeploymentType deploymentType;

    ApplicationVersion applicationVersion;

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

    @JsonView(Object.class)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "applicationVersionId", nullable = false)
    public ApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }



    @JsonView(Object.class)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "applicationEnvironmentId")
    public AstamApplicationEnvironment getApplicationEnvironment() {
        return applicationEnvironment;
    }

    public void setApplicationEnvironment(AstamApplicationEnvironment applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }
}
