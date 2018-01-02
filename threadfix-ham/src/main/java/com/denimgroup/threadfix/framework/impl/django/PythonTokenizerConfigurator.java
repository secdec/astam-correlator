package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizerConfigurator;

import java.io.StreamTokenizer;

public class PythonTokenizerConfigurator implements EventBasedTokenizerConfigurator {

    public static PythonTokenizerConfigurator INSTANCE = new PythonTokenizerConfigurator();

    @Override
    public void configure(StreamTokenizer tokenizer) {
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        tokenizer.eolIsSignificant(true);
        tokenizer.wordChars('_', '_');
        tokenizer.ordinaryChar('.');
        tokenizer.wordChars('.', '.');
        //tokenizer.commentChar('#');
        tokenizer.ordinaryChar(':');
        tokenizer.ordinaryChar('"');
        tokenizer.ordinaryChar('\'');
        tokenizer.wordChars('/', '/');
        tokenizer.wordChars('\\', '\\');


        //  Python uses whitespace for scoping
        tokenizer.ordinaryChar(' ');
        tokenizer.ordinaryChar('\t');
    }
}
