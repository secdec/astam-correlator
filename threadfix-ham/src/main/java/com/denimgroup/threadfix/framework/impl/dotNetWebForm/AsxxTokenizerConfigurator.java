package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizerConfigurator;

import java.io.StreamTokenizer;

// Used for As*x files, prevents use of backslash '\' as escape character
public class AsxxTokenizerConfigurator implements EventBasedTokenizerConfigurator {

    @Override
    public void configure(StreamTokenizer tokenizer) {
        tokenizer.ordinaryChar('\\');
        tokenizer.ordinaryChar('/');
        tokenizer.wordChars('\\', '\\');
    }
}
