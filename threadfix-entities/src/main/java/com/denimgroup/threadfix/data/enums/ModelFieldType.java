package com.denimgroup.threadfix.data.enums;

/**
 * Created by amohammed on 8/31/2017.
 */
public enum ModelFieldType {
    OTHER("Other"),
    STRING("String"),
    INTEGER("Integer");


    ModelFieldType(String displayName){ this.displayName = displayName;}

    private String displayName;

    public String getDisplayName(){ return displayName;}

    public static ModelFieldType getType(String input){
        ModelFieldType type = OTHER;

        if(input.equals("Integer") || input.equals("int")){
            type = INTEGER;
        } else if (input.equalsIgnoreCase("String")){
            type = STRING;
        }

        return type;
    }

}