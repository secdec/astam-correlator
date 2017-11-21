package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonClass;

import java.util.Collection;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class AdminSiteClass extends PythonClass {

    Map<String, String> registrations = map();

    @Override
    public String getName() {
        return "AdminSite";
    }

    public void register(String model, String adminClass) {
        // if adminClass is null, use default ModelAdmin
        if (adminClass == null) {
            adminClass = "django.contrib.admin.ModelAdmin";
        }

        registrations.put(model, adminClass);
    }
}
