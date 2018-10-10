////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


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