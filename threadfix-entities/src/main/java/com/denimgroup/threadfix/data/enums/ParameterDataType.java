package com.denimgroup.threadfix.data.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amohammed on 8/31/2017.
 */
public enum ParameterDataType {
    STRING("String"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    DECIMAL("Decimal"),
    LOCAL_DATE("LocalDate"),
    DATE_TIME("DateTime");

    ParameterDataType(String displayName){ this.displayName = displayName;}

    private String displayName;

    public String getDisplayName(){ return displayName;}

    public static ParameterDataType getType(String input){
        ParameterDataType type;

        if (input == null) {
            return STRING;
        }

        switch (input.toLowerCase()) {
            case "integer":
            case "int":
            case "long":
            case "short":
                type = INTEGER;
                break;
            case "decimal":
            case "float":
            case "double":
                type = DECIMAL;
                break;
            case "string":
                type = STRING;
                break;
            case "bool":
            case "boolean":
                type = BOOLEAN;
                break;
            case "localdate":
                type = LOCAL_DATE;
                break;
            case "datetime":
                type = DATE_TIME;
                break;
            default:
                type = STRING;
                break;
        }

        return type;
    }

}