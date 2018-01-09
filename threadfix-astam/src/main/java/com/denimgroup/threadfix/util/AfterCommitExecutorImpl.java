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

package com.denimgroup.threadfix.util;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;


@Component
public class AfterCommitExecutorImpl extends TransactionSynchronizationAdapter implements AfterCommitExecutor {

    private static final Logger logger = Logger.getLogger(AfterCommitExecutor.class);
    private static final ThreadLocal<List<Runnable>> RUNNABLES = new ThreadLocal<List<Runnable>>();

    @Override
    public void execute(Runnable runnable) {
        logger.info(String.format("Submitting new runnable %s to run after commit", runnable));
        if(!TransactionSynchronizationManager.isSynchronizationActive()){
            logger.info(String.format("Transaction synchronization is NOT ACTIVE. Executing right now runnable %s", runnable));
            runnable.run();
            return;
        }

        List<Runnable> threadRunnables = RUNNABLES.get();

        if(threadRunnables == null){
            threadRunnables = new ArrayList<Runnable>();
            RUNNABLES.set(threadRunnables);
            TransactionSynchronizationManager.registerSynchronization(this);
        }
        threadRunnables.add(runnable);

    }

    @Override
    public void afterCommit(){
        List<Runnable> threadRunnables = RUNNABLES.get();
        logger.info(String.format("Transaction successfully committed, executing %d runnables", threadRunnables.size()));

        for(Runnable runnable : threadRunnables){
            logger.info(String.format("Executing runnable %s", runnable));

            try{
                runnable.run();
            }catch(Exception e){
                logger.error(String.format("Failed to execute runnable %s", runnable), e);
            }
        }

    }

    @Override
    public void afterCompletion(int status){
        logger.info(String.format("Transaction completed with status %s", status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK"));
        RUNNABLES.remove();
    }
}
