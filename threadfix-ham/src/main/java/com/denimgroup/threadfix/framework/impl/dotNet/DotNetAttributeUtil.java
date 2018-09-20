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
