package com.denimgroup.threadfix.data.enums;

/**
 * Created by amohammed on 8/31/2017.
 */
public enum ParameterDataType {
    STRING("String"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    LOCAL_DATE("LocalDate");

    ParameterDataType(String displayName){ this.displayName = displayName;}

    private String displayName;

    public String getDisplayName(){ return displayName;}

    public static ParameterDataType getType(String input){
        ParameterDataType type;

        switch (input.toLowerCase()) {
            case "integer":
            case "int":
                type = INTEGER;
                break;
            case "string":
                type = STRING;
                break;
            case "boolean":
                type = BOOLEAN;
                break;
            case "localdate":
                type = LOCAL_DATE;
                break;
            default:
                type = STRING;
                break;
        }

        return type;
    }

}