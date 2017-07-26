package com.denimgroup.threadfix.data.entities.astam;

public enum AstamDeploymentType {

    UNDEFINED_TYPE(0),
    HEAD(10),
    DEVELOPMENT(20),
    SANDBOX(30),
    INTEGRATION(40),
    TEST(50),
    QUALITY_ASSURANCE(60),
    STAGING(70),
    PRE_PRODUCTION(80),
    DEMONSTRATION(90),
    PRODUCTION(100);

    private int value;

    AstamDeploymentType(int value){this.value = value;}

    public int getValue(){
        return this.value;
    }

    public static AstamDeploymentType getAstamDeploymentTypeByValue(int value) {
        for (AstamDeploymentType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}
