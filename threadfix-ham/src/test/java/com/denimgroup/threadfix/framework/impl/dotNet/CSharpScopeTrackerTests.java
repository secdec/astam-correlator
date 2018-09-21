package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.CSharpEventTokenizerConfigurator;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.CSharpScopeTracker;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import org.junit.Test;

public class CSharpScopeTrackerTests {

    private CSharpScopeTracker parseString(String code) {
        CSharpScopeTracker scopeTracker = new CSharpScopeTracker();
        EventBasedTokenizerRunner.runString(code, new CSharpEventTokenizerConfigurator(), scopeTracker);
        //  Do one more processToken call to allow states to finalize
        scopeTracker.processToken(0, code.replaceAll("[^\n]", "").length() + 1, null);
        return scopeTracker;
    }

    @Test
    public void testSimpleString() {
        CSharpScopeTracker scopeTracker = parseString("\"/*Foo*/ // ([{<\"");
        assert !scopeTracker.isInString() :
            "Expected not to be in string, was in string";
        assert !scopeTracker.isInComment() :
            "Expected not to be in comment, was in comment";
        assert scopeTracker.getNumOpenParen() == 0 :
            "Expected 0, got " + scopeTracker.getNumOpenParen();
        assert scopeTracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + scopeTracker.getNumOpenBrace();
        assert scopeTracker.getNumOpenBracket() == 0 :
            "Expected 0, got " + scopeTracker.getNumOpenBracket();
        assert scopeTracker.getNumOpenAngleBracket() == 0 :
            "Expected 0, got " + scopeTracker.getNumOpenAngleBracket();
    }

    @Test
    public void testSingleLineComment() {
        CSharpScopeTracker braceTracker = parseString("// {");
        assert braceTracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + braceTracker.getNumOpenBrace();
        assert braceTracker.isInComment() :
            "Expected to be in comment, was not";

        CSharpScopeTracker noBraceTracker = parseString("{\n//{\n}");
        assert noBraceTracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + noBraceTracker.getNumOpenBrace();
        assert !noBraceTracker.isInComment() :
            "Expected not to be in comment, was in comment";
    }

    @Test
    public void testMultiLineComment() {
        CSharpScopeTracker tracker = parseString("/* \"{ */");
        assert !tracker.isInComment() :
            "Expected not to be in comment, was in comment";
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
        assert tracker.getNumOpenBrace() == 0:
            "Expected 0, got " + tracker.getNumOpenBrace();

        CSharpScopeTracker multiLineTracker = parseString("/* ( \n ( \n ( */");
        assert !multiLineTracker.isInComment() :
            "Expected not to be in comment, was in comment";
        assert multiLineTracker.getNumOpenParen() == 0 :
            "Expected 0, got " + multiLineTracker.getNumOpenParen();
    }

    @Test
    public void testInterpolatedString() {
        CSharpScopeTracker tracker = parseString("$\"{test(\"\")}\"");
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
        assert tracker.getNumOpenParen() == 0 :
            "Expected 0, got " + tracker.getNumOpenParen();
        assert tracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + tracker.getNumOpenBrace();
    }

    @Test
    public void testVerbatimString() {
        CSharpScopeTracker tracker = parseString("@\"\\\"");
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
    }

    @Test
    public void testInterpolatedVerbatimString() {
        CSharpScopeTracker tracker = parseString("$@\"\\{test()}\\\"");
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
        assert tracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + tracker.getNumOpenBrace();
        assert tracker.getNumOpenParen() == 0 :
            "Expected 0, got " + tracker.getNumOpenParen();
    }

    @Test
    public void testMultiInterpolatedString() {
        CSharpScopeTracker tracker = parseString("$\"abc {test()} 123 {foo}\"");
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
        assert tracker.getNumOpenParen() == 0 :
            "Expected 0, got " + tracker.getNumOpenParen();
        assert tracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + tracker.getNumOpenBrace();
    }

    @Test
    public void testEmbeddedInterpolatedStrings() {
        CSharpScopeTracker tracker = parseString("$\"{test($\"{foo}\")}\"");
        assert !tracker.isInString() :
            "Expected not to be in string, was in string";
        assert tracker.getNumOpenBrace() == 0 :
            "Expected 0, got " + tracker.getNumOpenBrace();
        assert tracker.getNumOpenParen() == 0 :
            "Expected 0, got " + tracker.getNumOpenParen();
    }

}
