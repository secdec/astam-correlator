package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizerConfigurator;

import java.io.StreamTokenizer;

public class DjangoTokenizerConfigurator implements EventBasedTokenizerConfigurator {

    public static DjangoTokenizerConfigurator INSTANCE = new DjangoTokenizerConfigurator();

    @Override
    public void configure(StreamTokenizer tokenizer) {
        tokenizer.eolIsSignificant(true);
        tokenizer.wordChars('_', '_');
        tokenizer.commentChar('#');
    }
}
