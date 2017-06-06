package com.denimgroup.threadfix.data.dao.hibernate;

import com.denimgroup.threadfix.data.dao.AbstractObjectDao;
import com.denimgroup.threadfix.data.dao.SourceCodeStatusDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeRepoType;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class HibernateSourceCodeStatusDao extends AbstractObjectDao<SourceCodeStatus> implements SourceCodeStatusDao {

    @Autowired
    public HibernateSourceCodeStatusDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    @Override
    protected Class<SourceCodeStatus> getClassReference() {
        return SourceCodeStatus.class;
    }

    @Override
    public SourceCodeStatus getLatest(Application application) {
        return (SourceCodeStatus) sessionFactory
                .getCurrentSession()
                .createCriteria(SourceCodeStatus.class)
                .add(Restrictions.eq("application",application))
                .add(Restrictions.eq("repoType", SourceCodeRepoType.getType(application.getRepositoryType())))
                .addOrder(Order.asc("dateTimeChecked"))
                .setMaxResults(1)
                .uniqueResult();
    }

}
