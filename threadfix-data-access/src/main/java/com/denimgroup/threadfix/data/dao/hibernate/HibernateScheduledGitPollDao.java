package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.ScheduledGitPollDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class HibernateScheduledGitPollDao extends HibernateScheduledJobDao<ScheduledGitPoll> implements ScheduledGitPollDao {

    @Autowired
    public HibernateScheduledGitPollDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<ScheduledGitPoll> getClassReference() {
        return ScheduledGitPoll.class;
    }


    @Override
    public ScheduledGitPoll loadByApplication(Application application) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(ScheduledGitPoll.class);
        criteria.add(Restrictions.eq("active", true));
        criteria.add(Restrictions.eq("application", application));
        return (ScheduledGitPoll)criteria.uniqueResult();
    }
}
