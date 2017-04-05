package com.denimgroup.threadfix.data.dao;

import com.denimgroup.threadfix.data.entities.WebAttackSurface;

import java.util.List;

/**
 * Created by csotomayor on 3/3/2017.
 */
public interface WebAttackSurfaceDao extends GenericObjectDao<WebAttackSurface> {
    List<String> getFilePaths();
}
