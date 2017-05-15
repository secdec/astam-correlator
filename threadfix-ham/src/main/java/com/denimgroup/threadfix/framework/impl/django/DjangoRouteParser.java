package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

/**
 * Created by csotomayor on 5/12/2017.
 */
public class DjangoRouteParser implements EventBasedTokenizer{

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

    }
}
