package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

public enum DeviseModuleType {
    DEFAULT, // Sign in/out
    RECOVERABLE, // Password recovery
    CONFIRMABLE, // User confirmation
    REGISTERABLE, // User registartion
    LOCKABLE // User account locking
}
