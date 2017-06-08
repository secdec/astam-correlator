package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AbstractObjectDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<String> getFilePaths() {
        return sessionFactory.getCurrentSession()
                .createQuery("select dfe.sourceFileName from WebAttackSurface was " +
                        "join DataFlowElement dfe on was.dataFlowElement.id = dfe.id")
                .list();
    }

    @Override
    public List<WebAttackSurface> retrieveWebAttackSurfaceByAppId(int appId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(WebAttackSurface.class)
                .add(Restrictions.eq("application.id", appId));
        return criteria.list();
    }
}
