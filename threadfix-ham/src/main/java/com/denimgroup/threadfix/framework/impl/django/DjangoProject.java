////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.MemberExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoProject {

    private List<String> installedApps = list();
    private boolean appendSlash = false;
    private File rootDirectory = null;
    private AbstractPythonStatement settingsModule = null;

    private static String tryGetStringValue(PythonValue value, PythonInterpreter interpreter) {
        String result = null;

        if (value instanceof PythonUnresolvedValue) {
            PythonValue resolvedValue = interpreter.run(((PythonUnresolvedValue) value).getStringValue(), null, null);
            if (resolvedValue instanceof PythonIndeterminateValue) {
                result = ((PythonUnresolvedValue) value).getStringValue();
            } else {
                result = tryGetStringValue(resolvedValue, interpreter);
            }
        } else if (value instanceof PythonStringPrimitive) {
            result = ((PythonStringPrimitive) value).getValue();
        } else if (value instanceof PythonExpression) {
            PythonValue resolvedValue = interpreter.run((PythonExpression)value, null, null);
            result = tryGetStringValue(resolvedValue, interpreter);
        }

        return result;
    }

    public static DjangoProject loadFrom(File searchDirectory, final PythonCodeCollection codebase) {
        assert searchDirectory.isDirectory();

        DjangoProject result = new DjangoProject();
        result.rootDirectory = searchDirectory;

        final List<AbstractPythonStatement>
                installedAppsReferences = list(),
                djangoSettingsModuleReferences = list(),
                djangoSettingsConfigureReferences = list(),
                appendSlashReferences = list();

        codebase.traverse(new AbstractPythonVisitor() {

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                super.visitPublicVariable(pyVariable);
                String name = pyVariable.getName();

                if (name == null) {
                    return;
                }

                if (name.equals("INSTALLED_APPS")) {
                    installedAppsReferences.add(pyVariable);
                } else if (name.equals("DJANGO_SETTINGS_MODULE")) {
                    djangoSettingsModuleReferences.add(pyVariable);
                } else if (name.equals("APPEND_SLASH")) {
                    appendSlashReferences.add(pyVariable);
                }
            }

            @Override
            public void visitVariableModifier(PythonVariableModification pyModification) {
                super.visitVariableModifier(pyModification);
                if (pyModification.getResolvedTarget() == null) {
                    return;
                }

                String name = pyModification.getResolvedTarget().getName();
                if (name == null) {
                    return;
                }

                if (name.equals("INSTALLED_APPS")) {
                    installedAppsReferences.add(pyModification);
                } else if (name.equals("DJANGO_SETTINGS_MODULE")) {
                    djangoSettingsModuleReferences.add(pyModification);
                } else if (name.equals("APPEND_SLASH")) {
                    appendSlashReferences.add(pyModification);
                }
            }

            @Override
            public void visitFunctionCall(PythonFunctionCall pyFunctionCall) {
                super.visitFunctionCall(pyFunctionCall);

                String name = pyFunctionCall.getFunctionName();

                if (name == null) {
                    return;
                }

                name = codebase.expandSymbol(name, pyFunctionCall);
                if (name.equals("django.conf.settings.configure")) {
                    djangoSettingsConfigureReferences.add(pyFunctionCall);
                }
            }
        });

        PythonInterpreter interpreter = new PythonInterpreter(codebase);

        PythonValue
                bestInstalledAppsValue = null,
                bestSettingsModuleValue = null;

        boolean appendSlash = false;

        for (AbstractPythonStatement ref : installedAppsReferences) {
            String value = null;
            if (ref instanceof PythonPublicVariable) {
                value = ((PythonPublicVariable) ref).getValueString();
            } else {
                value = ((PythonVariableModification) ref).getOperatorValue();
            }

            if (value == null) {
                continue;
            }

            PythonValue interpretedValue = interpreter.run(value, null, null);

            boolean replace = false;
            if (interpretedValue instanceof PythonArray) {
                replace = true;
                if (bestInstalledAppsValue != null) {
                    if (((PythonArray)bestInstalledAppsValue).getEntries().size() < ((PythonArray) interpretedValue).getEntries().size()) {
                        replace = false;
                    }
                }
            }

            if (interpretedValue.getSourceLocation() == null) {
                interpretedValue.resolveSourceLocation(ref);
            }

            if (replace) {
                bestInstalledAppsValue = interpretedValue;
            }
        }

        for (AbstractPythonStatement ref : djangoSettingsModuleReferences) {
            String value = null;
            if (ref instanceof PythonPublicVariable) {
                value = ((PythonPublicVariable) ref).getValueString();
            } else {
                value = ((PythonVariableModification) ref).getOperatorValue();
            }

            if (value == null) {
                continue;
            }

            PythonValue interpretedValue = interpreter.run(value, null, null);

            if (interpretedValue.getSourceLocation() == null) {
                interpretedValue.resolveSourceLocation(ref);
            }

            if (interpretedValue instanceof PythonStringPrimitive) {
                bestSettingsModuleValue = interpretedValue;
            }
        }

        for (AbstractPythonStatement ref : appendSlashReferences) {
            String value = null;
            if (ref instanceof PythonPublicVariable) {
                value = ((PythonPublicVariable) ref).getValueString();
            } else {
                value = ((PythonVariableModification) ref).getOperatorValue();
            }

            if (value == null) {
                continue;
            }

            if (value.equals("True")) {
                appendSlash = true;
            } else if (value.equals("False")) {
                appendSlash = false;
            }
        }

        result.appendSlash = appendSlash;

        if (bestInstalledAppsValue != null) {
            List<PythonValue> appsArray = ((PythonArray) bestInstalledAppsValue).getValues();
            for (PythonValue appModule : appsArray) {
                String app = tryGetStringValue(appModule, interpreter);
                if (app != null) {
                    result.installedApps.add(app);
                }
            }
        }

        if (bestSettingsModuleValue != null) {
            String settingsModule = ((PythonStringPrimitive)bestSettingsModuleValue).getValue();
            result.settingsModule = codebase.resolveLocalSymbol(settingsModule, bestSettingsModuleValue.getSourceLocation());
        }

        return result;
    }

    public AbstractPythonStatement getSettingsModule() {
        return settingsModule;
    }

    public void setSettingsModule(AbstractPythonStatement settingsModule) {
        this.settingsModule = settingsModule;
    }

    public List<String> getInstalledApps() {
        return installedApps;
    }

    public boolean willAppendSlash() {
        return appendSlash;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setWillAppendSlash(boolean appendSlash) {
        this.appendSlash = appendSlash;
    }

    public void setInstalledApps(@Nonnull List<String> installedApps) {
        this.installedApps = installedApps;
    }

    public void addInstalledApp(@Nonnull String fullAppName) {
        this.installedApps.add(fullAppName);
    }
}
