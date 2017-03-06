package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AbstractObjectDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by csotomayor on 3/3/2017.
 */
@Repository
public class HibernateWebAttackSurfaceDao extends AbstractObjectDao<WebAttackSurface> implements WebAttackSurfaceDao{

    @Autowired
    public HibernateWebAttackSurfaceDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<WebAttackSurface> getClassReference() {
        return WebAttackSurface.class;
    }
}
