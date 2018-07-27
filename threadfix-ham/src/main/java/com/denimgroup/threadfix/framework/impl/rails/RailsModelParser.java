////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.data.enums.ParameterDataType.INTEGER;
import static com.denimgroup.threadfix.data.enums.ParameterDataType.STRING;

/**
 * Created by sgerick on 4/23/2015.
 */
public class RailsModelParser implements EventBasedTokenizer {

    private static final SanitizedLogger LOG = new SanitizedLogger("RailsParser");

    private enum ModelState {
        INIT, CLASS, ATTR_ACCESSOR, VALIDATES
    }

    private Map<String, Map<String, ParameterDataType>> models = map();
    private String modelName = "";
    //private List<String> modelAttributes = list();
    private Map<String, ParameterDataType> modelAttributes = map();

    private ModelState currentModelState = ModelState.INIT;

    public static Map<String, Map<String, ParameterDataType>> parse(@Nonnull File rootFile) {
        if (!rootFile.exists() || !rootFile.isDirectory()) {
            LOG.error("Root file not found or is not directory. Exiting.");
            return null;
        }
        File modelDir = new File(rootFile,"app/models");
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            LOG.error("{rootFile}/app/models/ not found or is not directory. Exiting.");
            return null;
        }
        String[] rubyExtension  = new String[] { "rb" };
        Collection<File> rubyFiles = FileUtils.listFiles(modelDir, rubyExtension, true);

        RailsModelParser parser = new RailsModelParser();
        for (File rubyFile : rubyFiles) {
            parser.modelName = "";
            parser.modelAttributes = map();
            EventBasedTokenizerRunner.runRails(rubyFile, true, false, parser);
            if (!parser.modelName.isEmpty()) {
                parser.models.put(parser.modelName, parser.modelAttributes);
            }
        }

        return parser.models;
    }


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        String charValue = null;
        if (type > 0)
            charValue = String.valueOf(Character.toChars(type));

//        System.err.println();
//        System.err.println("line="+lineNumber);
//        System.err.println("sTyp="+type);
//        System.err.println("sVal="+stringValue);
//        System.err.println("cVal="+charValue);

        switch (currentModelState) {
            case CLASS:
                processClass(type, stringValue, charValue);
                break;
            case ATTR_ACCESSOR:
                processAttrAccessible(type, stringValue, charValue);
                break;
            case VALIDATES:
                processValidation(type, stringValue, charValue);
                break;
        }

        if (stringValue != null) {
            String s = stringValue.toLowerCase();
            if (s.equals("class")) {
                currentModelState = ModelState.CLASS;

            } else if (s.equals("attr_accessible")) {
                currentModelState = ModelState.ATTR_ACCESSOR;

            } else if (s.equals("attr_accessor")) {
                currentModelState = ModelState.ATTR_ACCESSOR;

            } else if (s.equals("validates")){
                currentModelState = ModelState.VALIDATES;
            }
        }
    }

    private void processClass(int type, String stringValue, String charValue) {
        if (type == StreamTokenizer.TT_WORD && stringValue != null) {
            modelName = stringValue;
            modelName = stringValue.replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase();
            currentModelState = ModelState.INIT;
        }
    }

    private void processAttrAccessible(int type, String stringValue, String charValue) {
        if (type == StreamTokenizer.TT_WORD && stringValue.startsWith(":")
                && stringValue.length() > 1) {
            stringValue = stringValue.substring(1);
            modelAttributes.put(stringValue, STRING);
            return;
        } else if (",".equals(charValue)) {
            return;
        } else {
            currentModelState = ModelState.INIT;
            return;
        }
    }

    private enum ValidationState { START, FIELD, NUMERICALITY, OPEN_BRACKET, ONLY_INTEGER ,END}

    private static String
            NUMERICALITY = "numericality:",
            VALIDATES = "validates",
            TRUE = "true",
            FALSE = "false",
            ONLY_INTEGER = "only_integer:";


    private ValidationState currValidationState = ValidationState.START;
    private String fieldName = null;
    private String oneStringAgo = null;
    private int oneTypeAgo;

    private void processValidation(int type, String stringValue, String charValue){

        if(type == StreamTokenizer.TT_EOL && oneTypeAgo == StreamTokenizer.TT_EOL){
            currValidationState = ValidationState.END;
        }


        switch (currValidationState){
            case START:
                if (type == StreamTokenizer.TT_WORD && stringValue.startsWith(":")
                        && stringValue.length() > 1) {
                    fieldName = stringValue.substring(1);
                    currValidationState = ValidationState.FIELD;
                }

                break;
            case FIELD:
                if(type == StreamTokenizer.TT_WORD && NUMERICALITY.equals(stringValue)) {
                    currValidationState = ValidationState.NUMERICALITY;
                } else if ((type == StreamTokenizer.TT_WORD && VALIDATES.equals(stringValue))){
                currValidationState = ValidationState.END;
            }

                break;
            case NUMERICALITY:
                if(type == StreamTokenizer.TT_WORD && NUMERICALITY.equals(oneStringAgo)
                        && (TRUE.equals(stringValue) || FALSE.equals(stringValue))){

                    //if numericality in this case is set to true, the type can be an integer or float
                    ParameterDataType dataType = TRUE.equals(stringValue) ? INTEGER : STRING;
                    modelAttributes.put(fieldName, dataType);
                    currValidationState = ValidationState.END;

                } else if (type == 123){
                    currValidationState = ValidationState.OPEN_BRACKET;
                } else {
                    currValidationState = ValidationState.END;
                }

                break;
            case OPEN_BRACKET:
                if (type == StreamTokenizer.TT_WORD && ONLY_INTEGER.equals(stringValue)){
                    currValidationState = ValidationState.ONLY_INTEGER;
                } else if (type == 125){
                    currValidationState = ValidationState.END;
                }

                break;
            case ONLY_INTEGER:
                if(type == StreamTokenizer.TT_WORD && ONLY_INTEGER.equals(oneStringAgo)){

                    ParameterDataType dataType = TRUE.equals(stringValue) ? INTEGER : STRING;
                    modelAttributes.put(fieldName, dataType);
                    currValidationState = ValidationState.END;
                } else if (type == 125){
                    currValidationState = ValidationState.END;
                }

                break;
            case END:
                fieldName = null;
                oneStringAgo = null;
                oneTypeAgo = 0;
                currValidationState = ValidationState.START;
                currentModelState = ModelState.INIT;
                break;

        }

        if(currValidationState != ValidationState.END) {
            if (type == StreamTokenizer.TT_WORD) {
                oneStringAgo = stringValue;
            }

            oneTypeAgo = type;
        }

    }

}
