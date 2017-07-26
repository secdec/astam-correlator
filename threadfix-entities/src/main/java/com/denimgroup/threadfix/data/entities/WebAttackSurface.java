package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.entities.astam.AstamApplicationDeployment;
import com.denimgroup.threadfix.data.entities.astam.AstamAuditableEntity;

import javax.persistence.*;

/**
 * Created by jsemtner on 3/2/2017.
 */
@Entity
@Table(name = "WebAttackSurface")
public class WebAttackSurface extends AstamAuditableEntity {
    private SurfaceLocation surfaceLocation;
    private DataFlowElement dataFlowElement;
    private Application application;
    private AstamApplicationDeployment astamApplicationDeployment;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "surfaceLocationId")
    public SurfaceLocation getSurfaceLocation() {
        return surfaceLocation;
    }

    public void setSurfaceLocation(SurfaceLocation surfaceLocation) {
        this.surfaceLocation = surfaceLocation;
    }

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "dataFlowElementId")
    public DataFlowElement getDataFlowElement() {
        return dataFlowElement;
    }

    public void setDataFlowElement(DataFlowElement dataFlowElement) {
        this.dataFlowElement = dataFlowElement;
    }

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "applicationId")
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }


    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "astamApplicationDeploymentId")
    public AstamApplicationDeployment getAstamApplicationDeployment() {
        return astamApplicationDeployment;
    }

    public void setAstamApplicationDeployment(AstamApplicationDeployment astamApplicationDeployment) {
        this.astamApplicationDeployment = astamApplicationDeployment;
    }


}
