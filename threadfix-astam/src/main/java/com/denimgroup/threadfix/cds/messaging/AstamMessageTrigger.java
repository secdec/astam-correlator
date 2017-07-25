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

import com.denimgroup.threadfix.cds.service.AstamApplicationImporter;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.secdec.astam.common.messaging.Messaging;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction;
import com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.DATA_APPLICATION_REGISTRATION;

/**
 * Created by amohammed on 7/19/2017.
 * This class triggers actions based on messages broadcasted by the CDS
 */


public class AstamMessageTrigger implements ApplicationContextAware{

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamMessageTrigger.class);

    //TODO: Autowire this
    private static AstamApplicationImporter applicationImporter;


    private ApplicationContext applicationContext;

    public AstamMessageTrigger(){

    }

    public void parse(Messaging.AstamMessage message){
        Messaging.AstamMessage.DataMessage dataMessage = message.getDataMessage();
        if(dataMessage == null){
            return;
        }

        Messaging.AstamMessage.DataMessage.DataEntity dataEntity = dataMessage.getDataEntity();

        //currently we only need to listen to application registrations
        if(dataEntity == DATA_APPLICATION_REGISTRATION){
            DataAction dataAction = dataMessage.getDataAction();
            DataSetType dataSetType = dataMessage.getDataSetType();
            if(dataAction == DataAction.DATA_CREATE || dataAction == DataAction.DATA_UPDATE){


                if(dataSetType == DataSetType.DATA_SET_SINGLE || dataSetType == DataSetType.DATA_SET_COMPLETE){
                    List<String> uuids = dataMessage.getEntityIdsList();
                    applicationImporter.importApplications(uuids);
                }
            } else if(dataAction == DataAction.DATA_DELETE){
                if(dataSetType == DataSetType.DATA_SET_SINGLE || dataSetType == DataSetType.DATA_SET_COMPLETE) {
                    List<String> uuids = dataMessage.getEntityIdsList();
                    //applicationImporter.deleteApplications(uuids);
                }
            }
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(applicationImporter, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
    }
}
