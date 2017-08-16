// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

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
