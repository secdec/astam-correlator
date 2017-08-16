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

package com.denimgroup.threadfix.service.queue;

import com.denimgroup.threadfix.service.SourceCodeMonitorService;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.*;
import com.denimgroup.threadfix.service.repository.GitServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.jms.*;

/** @author jrios */
@Component
public class GitQueueListener implements MessageListener{

    protected final SanitizedLogger log = new SanitizedLogger(GitQueueListener.class);

    @Autowired @Qualifier(value = "gitSourceCodeMonitorServiceImpl")
    private SourceCodeMonitorService gitSourceCodeMonitorService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private GitServiceImpl gitService;

    @Override
    @Transactional
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage) {
                int applicationId = ((MapMessage)message).getInt("applicationId");
                Application application = applicationService.loadApplication(applicationId);
                if(application != null){
                    gitSourceCodeMonitorService.doCheck(application);
                }else{
                    log.error("Unable to load application : " + applicationId);
                }
            }else{
                log.error("Unsupported message class.");
            }
        } catch (Exception e) {
            log.warn("The JMS message threw an error.");
            e.printStackTrace();
        }
    }
}
