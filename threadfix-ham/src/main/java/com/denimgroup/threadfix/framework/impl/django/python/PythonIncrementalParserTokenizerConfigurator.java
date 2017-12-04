package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizerConfigurator;

import java.io.StreamTokenizer;

public class PythonIncrementalParserTokenizerConfigurator implements EventBasedTokenizerConfigurator {

    public static final PythonIncrementalParserTokenizerConfigurator INSTANCE = new PythonIncrementalParserTokenizerConfigurator();

    @Override
    public void configure(StreamTokenizer tokenizer) {
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        tokenizer.eolIsSignificant(true);
        tokenizer.wordChars('_', '_');
        tokenizer.ordinaryChar('.');
        tokenizer.commentChar('#');
        tokenizer.ordinaryChar('"');
        tokenizer.ordinaryChar('\'');
        tokenizer.wordChars('/', '/');
        tokenizer.wordChars('\\', '\\');


        //  Python uses whitespace for scoping
        tokenizer.ordinaryChar(' ');
        tokenizer.ordinaryChar('\t');
    }
}
