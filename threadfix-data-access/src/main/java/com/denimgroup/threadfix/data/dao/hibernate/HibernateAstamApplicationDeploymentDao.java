package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AstamApplicationDeploymentDao;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationDeployment;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationEnvironment;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by jrios on 7/26/2017.
 */
@Repository
public class HibernateAstamApplicationDeploymentDao implements AstamApplicationDeploymentDao{

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public AstamApplicationDeployment retrieveById(int id) {
        return (AstamApplicationDeployment)sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationDeployment.class)
                .add(Restrictions.eq("id",id))
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public List<AstamApplicationDeployment> retrieveAllActive() {
        List<AstamApplicationDeployment> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationDeployment.class)
                .add(Restrictions.eq("active", true))
                .list();
        return list;
    }

    @Override
    public List<AstamApplicationDeployment> retrieveAll() {
        List<AstamApplicationDeployment> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationDeployment.class)
                .list();
        return list;
    }

    @Override
    public void saveOrUpdate(AstamApplicationDeployment object) {
        sessionFactory.getCurrentSession().saveOrUpdate(object);
    }
}
