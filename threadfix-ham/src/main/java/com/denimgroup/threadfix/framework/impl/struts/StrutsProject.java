package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;

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
    List<StrutsKnownPlugins> plugins = list();

    ActionMapper strutsMapper;


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

    public void addPlugin(StrutsKnownPlugins plugin) {
        plugins.add(plugin);
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

    public Collection<StrutsKnownPlugins> getPlugins() {
        return plugins;
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
}
