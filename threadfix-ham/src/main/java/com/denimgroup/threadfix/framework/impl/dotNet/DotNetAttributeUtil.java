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

package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DotNetAttributeUtil {
    public static List<CSharpAttribute> findHttpAttributes(CSharpMethod method) {
        List<String> collectedAttributeNames = list();
        List<CSharpAttribute> result = method.getAttributes("HttpGet", "HttpPost", "HttpPut", "HttpPatch", "HttpDelete");
        for (CSharpAttribute attr : result) {
            collectedAttributeNames.add(attr.getName());
        }

        CSharpAttribute acceptVerbsAttribute = method.getAttribute("AcceptVerbs");
        if (acceptVerbsAttribute != null) {
            List<String> acceptedVerbs = list();
            //  If only one parameter, either a string name of an HTTP method or a set of bit-flags
            if (acceptVerbsAttribute.getParameters().size() == 1) {
                String verbsString = acceptVerbsAttribute.getParameterValue(0).getValue().toLowerCase();
                if (verbsString.startsWith("\"")) {
                    //  String value
                    acceptedVerbs.add(CodeParseUtil.trim(verbsString, "\""));
                } else {
                    //  Bit-flag of 'System.Web.Mvc.HttpVerbs'
                    String[] verbFlags = verbsString.split("\\|");
                    for (String flag : verbFlags) {
                        flag = flag.trim();
                        if (flag.endsWith("get")) {
                            acceptedVerbs.add("get");
                        } else if (flag.endsWith("post")) {
                            acceptedVerbs.add("post");
                        } else if (flag.endsWith("put")) {
                            acceptedVerbs.add("put");
                        } else if (flag.endsWith("patch")) {
                            acceptedVerbs.add("patch");
                        } else if (flag.endsWith("delete")) {
                            acceptedVerbs.add("delete");
                        }
                    }
                }
            } else {
                //  A list of string parameters
                for (CSharpParameter param : acceptVerbsAttribute.getParameters()) {
                    acceptedVerbs.add(param.getStringValue().toLowerCase());
                }
            }

            //  Have a list of lower-case HTTP verbs; re-format so that first character is upper-case
            //  and attempt to add them as individual attributes
            for (String verb : acceptedVerbs) {
                verb = Character.toUpperCase(verb.charAt(0)) + verb.substring(1);
                String attributeName = "Http" + verb;
                if (!collectedAttributeNames.contains(attributeName)) {
                    collectedAttributeNames.add(attributeName);
                    CSharpAttribute verbAttribute = new CSharpAttribute();
                    verbAttribute.setName(attributeName);
                    result.add(verbAttribute);
                }
            }
        }

        return result;
    }
}
