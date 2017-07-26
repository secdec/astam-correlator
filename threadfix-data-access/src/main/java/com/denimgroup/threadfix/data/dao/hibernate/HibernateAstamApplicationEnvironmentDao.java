package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AstamApplicationEnvironmentDao;
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
public class HibernateAstamApplicationEnvironmentDao implements AstamApplicationEnvironmentDao {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public AstamApplicationEnvironment retrieveById(int id) {
        return (AstamApplicationEnvironment)sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationEnvironment.class)
                .add(Restrictions.eq("id",id))
                .add(Restrictions.eq("active", true))
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public List<AstamApplicationEnvironment> retrieveAllActive() {
        List<AstamApplicationEnvironment> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationEnvironment.class)
                .add(Restrictions.eq("active", true))
                .list();
        return list;
    }

    @Override
    public List<AstamApplicationEnvironment> retrieveAll() {
        List<AstamApplicationEnvironment> list = sessionFactory.getCurrentSession()
                .createCriteria(AstamApplicationEnvironment.class)
                .add(Restrictions.eq("active", true))
                .list();
        return list;
    }

    @Override
    public void saveOrUpdate(AstamApplicationEnvironment object) {
        sessionFactory.getCurrentSession().saveOrUpdate(object);
    }

}
