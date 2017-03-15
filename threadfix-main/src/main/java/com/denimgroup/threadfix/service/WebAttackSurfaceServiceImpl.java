package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by csotomayor on 3/3/2017.
 */
@Service
@Transactional(readOnly = false)
public class WebAttackSurfaceServiceImpl implements WebAttackSurfaceService{

    @Autowired
    private WebAttackSurfaceDao webAttackSurfaceDao;

    @Override
    @Transactional(readOnly = false)
    public void storeWebAttackSurface(WebAttackSurface endpoint) {
        webAttackSurfaceDao.saveOrUpdate(endpoint);
    }
}
