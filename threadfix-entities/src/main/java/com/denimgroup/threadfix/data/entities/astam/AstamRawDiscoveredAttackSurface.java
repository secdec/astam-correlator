package com.denimgroup.threadfix.data.entities.astam;

/**
 * Created by amohammed on 7/27/2017.
 */

import com.denimgroup.threadfix.data.entities.WebAttackSurface;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * Created by amohammed on 7/27/2017.
 */
@Entity
public class AstamRawDiscoveredAttackSurface extends AstamAuditableEntity{


    private AstamApplicationDeployment astamApplicationDeployment;

    private List<WebAttackSurface> webAttackSurfaces;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "astamApplicationDeploymentId")
    public AstamApplicationDeployment getAstamApplicationDeployment() {
        return astamApplicationDeployment;
    }

    public void setAstamApplicationDeployment(AstamApplicationDeployment astamApplicationDeployment) {
        this.astamApplicationDeployment = astamApplicationDeployment;
    }


}