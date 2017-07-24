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

import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType;

import javax.annotation.Nonnull;
import javax.jms.*;
import java.util.List;

/**
 * Created by amohammed on 6/27/2017.
 */
public class AstamMessageProducer implements Runnable{

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamMessageProducer.class);

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private BytesMessage bytesMessage;

    private String topicString;
    private DataEntity dataEntity;
    private DataAction dataAction;
    private DataSetType dataSetType;
    private List<String> entityIds;

    public AstamMessageProducer(
            @Nonnull Connection connection,
            @Nonnull String topicString,
            @Nonnull DataEntity dataEntity,
            @Nonnull DataAction dataAction,
            @Nonnull DataSetType dataSetType,
            @Nonnull List<String> entityIds){

        this.connection = connection;
        this.topicString = topicString;
        this.dataEntity = dataEntity;
        this.dataAction = dataAction;
        this.dataSetType = dataSetType;
        this.entityIds = entityIds;
    }

    @Override
    public void run() {
        try {
            create();
        }catch (InvalidClientIDException ice){
            LOGGER.error("Error caused by invalid Client ID: ", ice);
        } catch (JMSException e) {
            LOGGER.error("JMS Exception error while trying to send message", e);
        }
    }


    public void create() throws JMSException {
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(topicString);
        bytesMessage = session.createBytesMessage();
        messageProducer = session.createProducer(destination);
        createMessage(dataEntity, dataAction, dataSetType, entityIds);

        LOGGER.info("Sending message to ASTAM: " + topicString + " for entity's: " + entityIds.toString());
        messageProducer.send(bytesMessage);
        session.close();
        connection.close();
    }
    private void createMessage(DataEntity dataEntity,
                               DataAction dataAction,
                               DataSetType dataSetType,
                               List<String> entityIds) throws JMSException{

        //TODO: add support here for PROC or CUSTOM messages if needed.
            DataMessage dataMessage = DataMessage.newBuilder()
                    .setDataEntity(dataEntity)
                    .setDataAction(dataAction)
                    .setDataSetType(dataSetType)
                     .addAllEntityIds(entityIds)
                    .build();

            if(dataMessage != null){
                try {
                    byte[] bytes = dataMessage.toByteArray();
                    bytesMessage.writeBytes(bytes);
                } catch (NullPointerException npe ){
                    LOGGER.error("NPE while trying to create ASTAM message to send to the CDS", npe);
                }
            }

    }

}
