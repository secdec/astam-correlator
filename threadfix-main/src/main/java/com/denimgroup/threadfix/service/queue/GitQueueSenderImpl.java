////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.service.queue;


import com.denimgroup.threadfix.data.entities.ExceptionLog;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.ExceptionLogService;
import com.denimgroup.threadfix.service.repository.GitServiceImpl;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.text.SimpleDateFormat;

@Service
@Transactional
public class GitQueueSenderImpl implements GitQueueSender {

    protected final SanitizedLogger log = new SanitizedLogger(GitQueueSenderImpl.class);
    private String jmsErrorString = "The JMS system encountered an error that prevented the message from being correctly created.";
    private static final SimpleDateFormat format = new SimpleDateFormat("MMM d, y h:mm:ss a");

    @Autowired
    GitServiceImpl gitService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ExceptionLogService exceptionLogService;


    @Override
    public void pollGitRepo(int applicationId) {
        MapMessage  gitPollMap = new ActiveMQMapMessage();
        log.info("Sending message to poll git application with id of " + applicationId + ".");
        try {
            gitPollMap.setString("applicationId", Integer.toString(applicationId));
            jmsTemplate.convertAndSend("gitRequestQueue", gitPollMap);
        } catch (JMSException e) {
            log.error(jmsErrorString);
            addExceptionLog(e);
        }
    }

    private void addExceptionLog(Exception e) {
        ExceptionLog exceptionLog = new ExceptionLog(e);
        exceptionLogService.storeExceptionLog(exceptionLog);
        log.error("Uncaught exception - logging at " + format.format(exceptionLog.getTime().getTime()) + ".");
    }

}
