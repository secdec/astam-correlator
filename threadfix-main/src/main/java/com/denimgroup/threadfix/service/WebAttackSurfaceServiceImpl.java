package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by csotomayor on 3/3/2017.
 */
@Service
public class WebAttackSurfaceServiceImpl implements WebAttackSurfaceService{

    @Autowired
    private WebAttackSurfaceDao webAttackSurfaceDao;
}
