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

package com.denimgroup.threadfix.cds.messaging;

import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.List;


/**
 * Created by amohammed on 6/28/2017.
 */
//TODO: same client id bug, when both a producer/s and subscriber/s are active (Done needs testing)
public class AstamMessageManager {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamMessageManager.class);

    private AstamMessageProducer messageProducer = null;
    private AstamMessageSubscriber messageSubscriber = null;

    private boolean isConfigured = false;
    private static AstamConfiguration astamConfig;
    private static Connection connection;


    public AstamMessageManager(AstamConfiguration astamConfiguration){
        astamConfig = astamConfiguration;
    }

    private void setupConnection(){

        String brokerUrl = astamConfig.getCdsBrokerUrl();
        if(StringUtils.isBlank(brokerUrl)){
            LOGGER.error("Can not setup messaging service, broker url not configured");
            isConfigured = false;
            return;
        }

        String compId = astamConfig.getCdsCompId();
        if(StringUtils.isBlank(compId)){
            LOGGER.error("Can not setup messaging service, component id not configured");
            isConfigured = false;
            return;
        }

        isConfigured = true;

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            connection = connectionFactory.createConnection();
            connection.setClientID(brokerUrl);
        } catch (JMSException e) {
            LOGGER.error("JMS Exception occurred while trying to setup connection with ASTAM message broker", e);
        }

    }

    public static void thread(@Nonnull Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public void notify(@Nonnull DataEntity dataEntity,
                       @Nonnull DataAction dataAction,
                       @Nonnull DataSetType dataSetType,
                       @Nonnull List<String> entityIds) {
        setupConnection();
        if(!isConfigured){
            return;
        }
        String topicString = createTopic(dataEntity, dataAction, dataSetType);
         messageProducer = new AstamMessageProducer(connection,
                 topicString,
                dataEntity,
                dataAction,
                dataSetType,
                entityIds);

        thread(messageProducer, false);
    }

    public void subscribe(@Nonnull DataEntity dataEntity,
                          @Nonnull DataAction dataAction,
                          @Nonnull DataSetType dataSetType){
        setupConnection();
        if(!isConfigured){
            return;
        }
        String topicString = createTopic(dataEntity, dataAction, dataSetType);
        messageSubscriber = new AstamMessageSubscriber(connection, topicString);
        thread(messageSubscriber, false);
    }

    private String createTopic(DataEntity dataEntity, DataAction dataAction, DataSetType dataSetType) {
        String messageType = "DATA";
        return messageType + "." + dataEntity.name() + "." + dataAction.name() + "." + dataSetType.name();
    }
}
