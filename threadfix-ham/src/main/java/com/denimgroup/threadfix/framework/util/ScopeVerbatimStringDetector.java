package com.denimgroup.threadfix.framework.util;

/**
 * Adds support for verbatim strings during scope tracking.
 *
 * Verbatim strings treat '\' as an ordinary character.
 */
public interface ScopeVerbatimStringDetector {
    boolean isInVerbatimString();
    void parseToken(int token);
}
