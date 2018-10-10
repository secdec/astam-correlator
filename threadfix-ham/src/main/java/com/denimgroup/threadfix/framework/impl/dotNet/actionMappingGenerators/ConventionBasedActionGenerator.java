package com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerMappings;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetParameterUtil;
import com.denimgroup.threadfix.framework.impl.dotNet.RouteParameterMap;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.set;
import static com.denimgroup.threadfix.CollectionUtils.setFrom;

class ConventionBasedActionGenerator {

    public DotNetControllerMappings generateForClass(CSharpClass csClass, RouteParameterMap fileParameters, List<String> modelTypeNames) {
        DotNetControllerMappings currentMappings = new DotNetControllerMappings(csClass.getFilePath());
        currentMappings.setControllerName(csClass.getName().substring(0, csClass.getName().length() - "Controller".length()));
        currentMappings.setNamespace(csClass.getNamespace());

        findAndAddConventionMethods(csClass, fileParameters, currentMappings, modelTypeNames);

        return currentMappings;
    }

    public String detectHttpAttributeByName(String actionName) {
        if (actionName.startsWith("Get")) {
            return "HttpGet";
        } else if (actionName.startsWith("Post")) {
            return "HttpPost";
        } else if (actionName.startsWith("Delete")) {
            return "HttpDelete";
        } else if (actionName.startsWith("Put")) {
            return "HttpPut";
        } else {
            return "HttpGet";
        }
    }

    private void findAndAddConventionMethods(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        findAndAddGetMethod(csClass, fileParameters, mappings, modelTypeNames);
        findAndAddGetAllMethod(csClass, fileParameters, mappings, modelTypeNames);
        findAndAddPostMethod(csClass, fileParameters, mappings, modelTypeNames);
        findAndAddPutMethod(csClass, fileParameters, mappings, modelTypeNames);
        findAndAddDeleteMethod(csClass, fileParameters, mappings, modelTypeNames);
    }

    private void findAndAddGetMethod(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        CSharpMethod bestCandidate = null;

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            bestCandidate = selectBetterConventionCandidate(bestCandidate, method, "HttpGet", "Get", true);
        }

        if (bestCandidate == null) {
            return;
        }

        if (bestCandidate.getParameters().isEmpty()) {
            return;
        }

        String explicitRoute = "{" + bestCandidate.getParameters().get(0).getName() + "}";

        mappings.addAction(
            bestCandidate.getName(),
            set("HttpGet"),
            bestCandidate.getStartLine(),
            bestCandidate.getEndLine(),
            // params
            setFrom(DotNetParameterUtil.getMergedMethodParameters(bestCandidate, fileParameters, modelTypeNames)),
            explicitRoute,
            bestCandidate,
            true
        );
    }

    private void findAndAddGetAllMethod(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        CSharpMethod bestCandidate = null;

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            bestCandidate = selectBetterConventionCandidate(bestCandidate, method, "HttpGet", "Get", false);
        }

        if (bestCandidate == null) {
            return;
        }

        mappings.addAction(
            bestCandidate.getName(),
            set("HttpGet"),
            bestCandidate.getStartLine(),
            bestCandidate.getEndLine(),
            setFrom(DotNetParameterUtil.getMergedMethodParameters(bestCandidate, fileParameters, modelTypeNames)),
            "",
            bestCandidate,
            true
        );
    }

    private void findAndAddPostMethod(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        CSharpMethod bestCandidate = null;

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            bestCandidate = selectBetterConventionCandidate(bestCandidate, method, "HttpPost", "Post", true);
        }

        if (bestCandidate == null) {
            return;
        }

        mappings.addAction(
            bestCandidate.getName(),
            set("HttpPost"),
            bestCandidate.getStartLine(),
            bestCandidate.getEndLine(),
            setFrom(DotNetParameterUtil.getMergedMethodParameters(bestCandidate, fileParameters, modelTypeNames)),
            "",
            bestCandidate,
            true
        );
    }

    private void findAndAddPutMethod(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        CSharpMethod bestCandidate = null;

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            bestCandidate = selectBetterConventionCandidate(bestCandidate, method, "HttpPut", "Put", true);
        }

        if (bestCandidate == null) {
            return;
        }

        mappings.addAction(
            bestCandidate.getName(),
            set("HttpPut"),
            bestCandidate.getStartLine(),
            bestCandidate.getEndLine(),
            setFrom(DotNetParameterUtil.getMergedMethodParameters(bestCandidate, fileParameters, modelTypeNames)),
            "{id}",
            bestCandidate,
            true
        );
    }

    private void findAndAddDeleteMethod(CSharpClass csClass, RouteParameterMap fileParameters, DotNetControllerMappings mappings, List<String> modelTypeNames) {
        CSharpMethod bestCandidate = null;

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            bestCandidate = selectBetterConventionCandidate(bestCandidate, method, "HttpDelete", "Delete", true);
        }

        if (bestCandidate == null) {
            return;
        }

        mappings.addAction(
            bestCandidate.getName(),
            set("HttpDelete"),
            bestCandidate.getStartLine(),
            bestCandidate.getEndLine(),
            setFrom(DotNetParameterUtil.getMergedMethodParameters(bestCandidate, fileParameters, modelTypeNames)),
            "{id}",
            bestCandidate,
            true
        );
    }

    private CSharpMethod selectBetterConventionCandidate(CSharpMethod old, CSharpMethod candidate, String expectedAttribute, String conventionalName, boolean expectParameter) {
        if (candidate == null || !isRelevantConventionCandidate(candidate, expectedAttribute, conventionalName, expectParameter)) {
            return old;
        }

        if (old == null) {
            return candidate;
        }

        //  Pick the candidate that better meets the parameter expectations
        if (expectParameter) {
            CSharpParameter oldParameter = old.getParameters().isEmpty() ? null : old.getParameters().get(0);
            CSharpParameter candidateParameter = candidate.getParameters().isEmpty() ? null : candidate.getParameters().get(0);

            if ((oldParameter == null) != (candidateParameter == null)) {
                if (oldParameter == null) {
                    return candidate;
                } else {
                    return old;
                }
            }

            if (oldParameter != null) {
                if ((oldParameter.getDefaultValue() == null) != (candidateParameter.getDefaultValue() == null)) {
                    //  We require a parameter and one of these options has an optional parameter, the other does not
                    //      Select the method with the required parameter

                    if (oldParameter.getDefaultValue() != null) {
                        return old;
                    } else {
                        return candidate;
                    }
                }
            }
        } else {
            //  No parameter expected, if either has a parameter (even with default value)
            //  then pick the option without any parameters
            if (old.getParameters().size() < candidate.getParameters().size()) {
                return old;
            } else if (candidate.getParameters().size() < old.getParameters().size()) {
                return candidate;
            }
        }


        //  Pick the candidate that has the exact expected attribute
        if ((old.getAttribute(expectedAttribute) == null) != (candidate.getAttribute(expectedAttribute) == null)) {
            //  One has the attribute and the other doesn't
            if (old.getAttribute(expectedAttribute) != null) {
                return old;
            } else {
                return candidate;
            }
        }



        //  Pick the candidate that matches the naming convention
        if (old.getName().startsWith(conventionalName) != candidate.getName().startsWith(conventionalName)) {
            if (old.getName().startsWith(conventionalName)) {
                return old;
            } else {
                return candidate;
            }
        }



        //  Pick the candidate that has the shorter name, while sticking with the naming convention
        if (old.getName().startsWith(conventionalName) && candidate.getName().startsWith(conventionalName)) {
            if (old.getName().length() < candidate.getName().length()) {
                return old;
            } else {
                return candidate;
            }
        }


        //  No clear winner, keep the original
        return old;
    }

    private boolean isRelevantConventionCandidate(CSharpMethod method, String expectedAttributeName, String conventionalName, boolean expectParameter) {
        //  Methods with explicit ActionNames do not follow convention
        if (method.getAttribute("ActionName") != null || method.getAttribute("NonAction") != null || method.getAttribute("Route") != null) {
            return false;
        }

        //  An explicit [HttpX] attribute automatically makes it relevant
        CSharpAttribute expectedAttribute = method.getAttribute(expectedAttributeName);
        if (expectedAttribute != null) {
            //  An [HttpX] attribute with a parameter specifies an explicit route for this action;
            //  explicit routes cannot be used for convention-based actions
            return expectedAttribute.getParameters().isEmpty();
        }

        //  Relevant if it follows naming conventions
        return method.getName().startsWith(conventionalName);
    }
}
