package com.denimgroup.threadfix.framework.util;

import java.io.StreamTokenizer;

public class NoOpTokenizerConfigurator implements EventBasedTokenizerConfigurator {
    private NoOpTokenizerConfigurator() {

    }

    private static NoOpTokenizerConfigurator instance = new NoOpTokenizerConfigurator();
    public static NoOpTokenizerConfigurator getInstance() {
        return instance;
    }

    @Override
    public void configure(StreamTokenizer tokenizer) {

    }
}
