package com.denimgroup.threadfix.data.entities;

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
    private ApplicationVersion applicationVersion;

    @OneToOne
    @JoinColumn(name = "surfaceLocationId")
    public SurfaceLocation getSurfaceLocation() {
        return surfaceLocation;
    }

    public void setSurfaceLocation(SurfaceLocation surfaceLocation) {
        this.surfaceLocation = surfaceLocation;
    }

    @OneToOne
    @JoinColumn(name = "dataFlowElementId")
    public DataFlowElement getDataFlowElement() {
        return dataFlowElement;
    }

    public void setDataFlowElement(DataFlowElement dataFlowElement) {
        this.dataFlowElement = dataFlowElement;
    }

    @ManyToOne
    @JoinColumn(name = "applicationVersionId")
    public ApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
}
