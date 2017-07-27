package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AstamRawDiscoveredAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.astam.AstamRawDiscoveredAttackSurface;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by amohammed on 7/27/2017.
 */

@Repository
public class HibernateRawDiscoveredAttackSurfaceDao implements AstamRawDiscoveredAttackSurfaceDao {



    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public AstamRawDiscoveredAttackSurface retrieveById(int id) {
        return (AstamRawDiscoveredAttackSurface)sessionFactory.getCurrentSession()
                .createCriteria(AstamRawDiscoveredAttackSurface.class)
                .add(Restrictions.eq("id",id))
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public List<AstamRawDiscoveredAttackSurface> retrieveAllActive() {
        List<AstamRawDiscoveredAttackSurface> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamRawDiscoveredAttackSurface.class)
                .add(Restrictions.eq("active", true))
                .list();
        return list;
    }

    @Override
    public List<AstamRawDiscoveredAttackSurface> retrieveAll() {
        List<AstamRawDiscoveredAttackSurface> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamRawDiscoveredAttackSurface.class)
                .list();
        return list;
    }

    @Override
    public void saveOrUpdate(AstamRawDiscoveredAttackSurface object) {
        sessionFactory.getCurrentSession().saveOrUpdate(object);
    }
}
