package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.entities.astam.AstamAuditableEntity;
import com.denimgroup.threadfix.data.entities.astam.AstamRawDiscoveredAttackSurface;

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
    private AstamRawDiscoveredAttackSurface astamRawDiscoveredAttackSurface;

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


    //TODO:  remove this, have one RawDiscoveredAttackSurface hold references to multiple WebattackSurfaces
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "astamAstamRawDiscoveredAttackSurfaceId")
    public AstamRawDiscoveredAttackSurface getAstamRawDiscoveredAttackSurface() {
        return astamRawDiscoveredAttackSurface;
    }

    public void setAstamRawDiscoveredAttackSurface(AstamRawDiscoveredAttackSurface astamRawDiscoveredAttackSurface) {
        this.astamRawDiscoveredAttackSurface = astamRawDiscoveredAttackSurface;
    }

}
