package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Scan;

/**
 * Created by csotomayor on 3/3/2017.
 */
public interface WebAttackSurfaceService {
    void storeWebAttackSurface(Application application, Scan scan);
}
