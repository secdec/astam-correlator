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
import com.denimgroup.threadfix.data.dao.AstamConfigurationDao;
import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by amohammed on 7/23/2017.
 */

@Repository
@Transactional
public class HibernateAstamConfigurationDao
        extends AbstractObjectDao<AstamConfiguration>
        implements AstamConfigurationDao {


    @Autowired
    public HibernateAstamConfigurationDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<AstamConfiguration> getClassReference() {
        return AstamConfiguration.class;
    }

    @Override
    public void delete(AstamConfiguration config) {
        sessionFactory.getCurrentSession().delete(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AstamConfiguration> retrieveAll() {
        return getSession().createQuery("from AstamConfiguration").list();
    }


    @Override
    public AstamConfiguration loadCurrentConfiguration() {
        AstamConfiguration configuration;

        List<AstamConfiguration> list = retrieveAll();
        if (list.size() == 0) {
            configuration = AstamConfiguration.getInitialConfig();
        } else if (list.size() > 1) {
            AstamConfiguration config = list.get(0);
            list.remove(0);
            for (AstamConfiguration defaultConfig : list) {
                delete(defaultConfig);
            }
            configuration = config;
        } else {
            configuration = list.get(0);
        }

        return  configuration;
    }


}
