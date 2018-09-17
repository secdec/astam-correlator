package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizerConfigurator;

import java.io.StreamTokenizer;

//  Java StreamTokenizer behaves weirdly with slashes in strings and will incorrectly detect them as
//  comments; disable comment checking and manually do this in CSharpScopeTracker for generic C#
//  parsing in 'classParsers' package.
public class CSharpEventTokenizerConfigurator implements EventBasedTokenizerConfigurator {
    @Override
    public void configure(StreamTokenizer tokenizer) {
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);

        tokenizer.ordinaryChar('/');

        //  Disable tokenizer quotes detection since it doesn't
        //  play nice with C# interpolated strings
        tokenizer.ordinaryChar('"');
        tokenizer.ordinaryChar('\'');
    }
}
