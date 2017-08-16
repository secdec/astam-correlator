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
                .add(Restrictions.eq("branch",application.getResolvedRepositoryBranch()))
                .addOrder(Order.asc("dateTimeChecked"))
                .setMaxResults(1)
                .uniqueResult();
    }

}
