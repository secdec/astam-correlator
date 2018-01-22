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

package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsResult;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPlugin;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsProject {

    List<StrutsPackage> packages = list();
    List<StrutsAction> actions = list();
    List<StrutsClass> classes = list();
    String webPath = null;
    String webInfPath = null;
    StrutsConfigurationProperties config;
    List<StrutsPlugin> plugins = list();
    List<StrutsWebPack> webPacks = list();
    String rootDirectory = null;


    public StrutsProject(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }


    public void addPackages(Collection<StrutsPackage> packages) {
        this.packages.addAll(packages);
    }

    public void addActions(Collection<StrutsAction> actions) {
        this.actions.addAll(actions);
    }

    public void addClasses(Collection<StrutsClass> classes) {
        this.classes.addAll(classes);
    }

    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }

    public void setWebInfPath(String webInfPath) {
        this.webInfPath = webInfPath;
    }

    public void setConfiguration(StrutsConfigurationProperties config) {
        this.config = config;
    }

    public void addPlugin(StrutsPlugin plugin) {
        plugins.add(plugin);
    }

    public void addWebPack(StrutsWebPack webPack) {
        webPacks.add(webPack);
    }




    public Collection<StrutsPackage> getPackages() {
        return this.packages;
    }

    public Collection<StrutsAction> getActions() {
        return this.actions;
    }

    public Collection<StrutsClass> getClasses() {
        return this.classes;
    }

    public String getWebPath() {
        return this.webPath;
    }

    public String getWebInfPath() {
        return this.webInfPath;
    }

    public StrutsConfigurationProperties getConfig() {
        return config;
    }

    public Collection<StrutsPlugin> getPlugins() {
        return plugins;
    }

    public Collection<StrutsWebPack> getWebPacks() {
        return webPacks;
    }

    public String getRootDirectory() {
        return this.rootDirectory;
    }

    public boolean hasPlugin(Class cls) {
        for (StrutsPlugin plugin : plugins) {
            if (plugin.getClass() == cls) {
                return true;
            }
        }
        return false;
    }


    public StrutsWebPack findWebPack(String packRootPathRelativeToWebRoot) {
        String[] rootPathParts = packRootPathRelativeToWebRoot.split("\\/");
        StrutsWebPack result = null;
        for (StrutsWebPack webPack : webPacks) {
            String packRelativePath = webPack.getRootDirectoryPath().replace(webPath, "");
            if (!packRelativePath.startsWith("/")) {
                packRelativePath = "/" + packRelativePath;
            }
            String[] packPathParts = packRelativePath.split("\\/");
            if (rootPathParts.length != packPathParts.length) {
                continue;
            }

            boolean isMatch = true;
            for (int i = 0; i < packPathParts.length; i++) {
                if (!packPathParts[i].equalsIgnoreCase(rootPathParts[i])) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                result = webPack;
                break;
            }
        }

        return result;
    }

    //  Returns the location of the given web pack relative to the global web content directory
    public String getWebPackPathRelativeToWebRoot(StrutsWebPack webPack) {
        String webPackPath = webPack.getRootDirectoryPath();
        String relativePath = webPackPath.replace("^" + webPackPath, "");
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        return relativePath;
    }



    public StrutsClass findClassByFileLocation(String fileLocation) {
        for (StrutsClass strutsClass : classes) {
            if (strutsClass.getSourceFile().equalsIgnoreCase(fileLocation)) {
                return strutsClass;
            }
        }
        return null;
    }


    public StrutsClass findClassByName(String className) {
        for (StrutsClass strutsClass : classes) {
            if (strutsClass.getName().equalsIgnoreCase(className) ||
                    (strutsClass.getPackage() + "." + strutsClass.getName()).equalsIgnoreCase(className)) {
                return strutsClass;
            }
        }
        return null;
    }



    public StrutsPackage findPackageForAction(StrutsAction action) {
        for (StrutsPackage strutsPackage : packages) {
            if (strutsPackage.getActions().contains(action)) {
                return strutsPackage;
            }
        }
        return null;
    }



    public Collection<StrutsAction> findActionsForResultFile(String resultFilePath) {
        List<StrutsAction> actions = list();
        for (StrutsAction action : actions) {
            for (StrutsResult result : action.getResults()) {
                if (resultFilePath.endsWith(result.getValue())) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }
}
