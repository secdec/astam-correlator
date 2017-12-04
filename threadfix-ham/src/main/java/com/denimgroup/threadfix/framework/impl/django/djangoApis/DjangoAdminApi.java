package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteClass;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteRegisterFunction;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteUrlsVariable;
import com.denimgroup.threadfix.framework.impl.django.python.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class DjangoAdminApi extends AbstractDjangoApi {
    @Override
    public String getIdentifier() {
        return "django.contrib.admin";
    }

    @Override
    public void apply(PythonCodeCollection codebase) {
        PythonModule admin = makeModulesFromFullName("django.contrib.admin");

        attachModelAdmin(admin);
        attachAdminSite(admin);
        attachGlobalSite(admin);

        AbstractPythonStatement result = getRootScope(admin);
        tryAddScopes(codebase, result);
    }

    private PythonDecorator getAdminRegister(Collection<PythonDecorator> decorators) {
        for (PythonDecorator decorator : decorators) {
            if (decorator.getName().equals("register") || decorator.getName().equals("admin.register")) {
                return decorator;
            }
        }
        return null;
    }

    @Override
    public void applyPostLink(PythonCodeCollection codebase) {

        final Map<AbstractPythonStatement, PythonDecorator> decoratedModels = map();
        codebase.traverse(new AbstractPythonVisitor() {
            @Override
            public void visitClass(PythonClass pyClass) {
                super.visitClass(pyClass);
                PythonDecorator registerDecorator = getAdminRegister(pyClass.getDecorators());
                if (registerDecorator != null) {
                    decoratedModels.put(pyClass, registerDecorator);
                }
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                super.visitFunction(pyFunction);
                PythonDecorator registerDecorator = getAdminRegister(pyFunction.getDecorators());
                if (registerDecorator != null) {
                    decoratedModels.put(pyFunction, registerDecorator);
                }
            }
        });

        PythonPublicVariable site = codebase.findByFullName("django.contrib.admin.site", PythonPublicVariable.class);
        AdminSiteRegisterFunction registerFunction = codebase.findByFullName("django.contrib.admin.AdminSite.register", AdminSiteRegisterFunction.class);

        for (Map.Entry<AbstractPythonStatement, PythonDecorator> entry : decoratedModels.entrySet()) {
            AbstractPythonStatement admin = entry.getKey();
            PythonDecorator modelDecorator = entry.getValue();

            List<String> params = modelDecorator.getParams();
            if (params.size() != 1) {
                continue;
            }

            String modelName = params.get(0);
            String adminName = admin.getName();

            registerFunction.invoke(codebase, admin, site, new String[] { modelName, adminName });

        }

    }

    private Collection<String> parseInheritedAdminEndpoints(PythonCodeCollection codebase, PythonPublicVariable sitesVariable) {
        // Any implementor of ModelAdmin can provide a get_urls method which is automatically called and inserted as sub-paths
        // for that admin page.

        String originalUrlsString = sitesVariable.getValueString();
        originalUrlsString = originalUrlsString.substring(1, originalUrlsString.length() - 1);

        String[] existingUrls = CodeParseUtil.splitByComma(originalUrlsString);

        List<String> urlStrings = list();

        for (String existingUrl : existingUrls) {
            String[] urlParams = CodeParseUtil.splitByComma(existingUrl);
            String endpoint = urlParams[0];
            String controllerName = urlParams[1];

            PythonClass controller = codebase.findByFullName(controllerName, PythonClass.class);
            if (controller == null) {
                continue;
            }

            AbstractPythonStatement get_urlsFunction = controller.findChild("get_urls");
            if (get_urlsFunction == null) {
                continue;
            }

            Collection<String> currentUrls = parseUrlsFunction(codebase, (PythonFunction)get_urlsFunction);
            if (currentUrls != null) {
                urlStrings.addAll(currentUrls);
            }
        }



        return urlStrings;
    }

    private Collection<String> parseUrlsFunction(PythonCodeCollection codebase, PythonFunction function) {
        // A basic single-depth search for URL objects, would optimally traverse and evaluate all referenced functions
        File codeFile = new File(function.getSourceCodePath());

        final Collection<PythonLambda> localLambdas = list();

        // This value resolution logic has been implemented specifically for the Django Admin API but
        // is intended to be abstracted for general use. This includes resolving lambdas, function
        // return values, and modifiers ie concatenation/interpolation.

        function.accept(new AbstractPythonVisitor() {
            @Override
            public void visitLambda(PythonLambda pyLambda) {
                super.visitLambda(pyLambda);
                localLambdas.add(pyLambda);
            }
        });
        try {
            FileReader fileReader = new FileReader(codeFile);
            BufferedReader reader = new BufferedReader(fileReader);

            int startLine = function.getSourceCodeStartLine();
            int endLine = function.getSourceCodeEndLine();

            for (int lineno = 0; lineno < endLine; lineno++) {
                String currentLine = reader.readLine();

                if (lineno < startLine) {
                    continue;
                }

                // Replace lambda references with their whole expression
                // NOTE: This won't work for multiline URL declarations!
                for (PythonLambda lambda : localLambdas) {
                    String name = lambda.getName();
                    String keyword = name + "(";
                    int referenceStartIndex = currentLine.indexOf(keyword);
                    if (referenceStartIndex < 0) {
                        continue;
                    }

                    String params = null;

                    // Extract full function parameters
                    int numOpenParen = 0, numOpenBrace = 0, numOpenBracket = 0;
                    int lastChar = -1, startQuoteType = -1;
                    boolean isInString = false;
                    // This sort of scoping/string detection should be generalized
                    for (int i = referenceStartIndex + keyword.length(); i < currentLine.length(); i++) {

                        char c = currentLine.charAt(i);
                        if (c == '"' || c == '\'') {
                            if (isInString) {
                                if (c == startQuoteType && lastChar != '\\') {
                                    startQuoteType = -1;
                                    isInString = false;
                                }
                            } else {
                                startQuoteType = c;
                                isInString = true;
                            }
                        }

                        if (!isInString) {

                            if (c == '(') numOpenParen++;
                            if (c == '[') numOpenBracket++;
                            if (c == '{') numOpenBrace++;
                            if (c == ')') numOpenParen--;
                            if (c == ']') numOpenBracket--;
                            if (c == '}') numOpenBrace--;

                            if (c == ')' && numOpenParen == 0 && numOpenBrace == 0 && numOpenBracket == 0) {
                                params = currentLine.substring(referenceStartIndex + keyword.length(), i);
                                break;
                            }
                        }

                        lastChar = c;
                    }

                    if (params == null) {
                        continue;
                    }

                    String[] paramParts = CodeParseUtil.splitByComma(params);
                    String lambdaBody = lambda.getFunctionBody();

                    int p = 0;
                    for (String paramName : lambda.getParamNames()) {
                        lambdaBody = lambdaBody.replace(paramName, paramParts[p]);
                    }

                    // WIP
                    //currentLine = currentLine.replace()

                }
            }




        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void attachModelAdmin(AbstractPythonStatement target) {
        PythonClass modelAdmin = new PythonClass();
        modelAdmin.setName("ModelAdmin");
        target.addChildStatement(modelAdmin);
    }

    private void attachAdminSite(AbstractPythonStatement target) {
        AdminSiteClass adminSite = new AdminSiteClass();
        PythonFunction register = new AdminSiteRegisterFunction();
        AdminSiteUrlsVariable urls = new AdminSiteUrlsVariable();
        adminSite.addChildStatement(register);
        adminSite.addChildStatement(urls);

        target.addChildStatement(adminSite);
    }

    private void attachGlobalSite(AbstractPythonStatement target) {
        PythonPublicVariable site = new PythonPublicVariable();
        site.setName("site");
        site.setValueString("django.contrib.admin.AdminSite()");

        target.addChildStatement(site);
    }


}
