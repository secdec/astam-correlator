package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.*;
import com.denimgroup.threadfix.framework.impl.django.python.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
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
    public void applySchema(PythonCodeCollection codebase) {
        PythonModule admin = makeModulesFromFullName("django.contrib.admin");

        attachModelAdmin(admin);
        attachAdminSite(admin);
        attachGlobalSite(admin);
        attachFunctions(admin);

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
    public void applySchemaPostLink(PythonCodeCollection codebase) {

    }

    @Override
    public void applyRuntime(final PythonInterpreter runtime) {
        ExecutionContext executionContext = runtime.getRootExecutionContext();
        PythonCodeCollection codebase = executionContext.getCodebase();

        AdminSiteClass adminSiteClass = codebase.findByFullName("django.contrib.admin.AdminSite", AdminSiteClass.class);
        final PythonFunction adminInit = adminSiteClass.findChild("__init__", PythonFunction.class);

        PythonValue commonSite = new PythonObject(adminSiteClass);
        commonSite.resolveSourceLocation(codebase.findByFullName("django.contrib.admin.site"));


        runtime.pushExecutionContext(adminInit, commonSite);

        adminInit.invoke(runtime, null, null);

        executionContext.assignSymbolValue("django.contrib.admin.site", commonSite);

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

        PythonFunction registerFunction = adminSiteClass.findChild("register", PythonFunction.class);



        for (Map.Entry<AbstractPythonStatement, PythonDecorator> entry : decoratedModels.entrySet()) {
            AbstractPythonStatement admin = entry.getKey();
            PythonDecorator modelDecorator = entry.getValue();

            List<String> params = modelDecorator.getParams();
            if (params.size() != 1) {
                continue;
            }

            String modelName = params.get(0);
            String adminName = admin.getName();

            registerFunction.invoke(runtime, admin, new PythonValue[] {
                    new PythonVariable(modelName, codebase.resolveLocalSymbol(modelName, admin)),
                    new PythonVariable(adminName, admin)
            });
        }

        runtime.popExecutionContext();
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
        adminSite.addChildStatement(new AdminSiteInit());

        target.addChildStatement(adminSite);
    }

    private void attachGlobalSite(AbstractPythonStatement target) {
        PythonPublicVariable site = new PythonPublicVariable();
        site.setName("site");
        site.setValueString("django.contrib.admin.AdminSite()");

        target.addChildStatement(site);
    }

    private void attachFunctions(AbstractPythonStatement target) {
        AdminSiteAdminViewFunction adminViewFunction = new AdminSiteAdminViewFunction();
        target.addChildStatement(adminViewFunction);
    }

}
