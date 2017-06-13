package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.ScheduledGitPollDao;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;
import org.hibernate.SessionFactory;
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


}
