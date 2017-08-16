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
