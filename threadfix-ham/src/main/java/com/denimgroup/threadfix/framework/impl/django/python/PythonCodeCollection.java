package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonCodeCollection {

    private static SanitizedLogger LOG = new SanitizedLogger(PythonCodeCollection.class);

    List<AbstractPythonScope> scopes = list();

    private void log(String message) {
        LOG.debug(message);
        //LOG.info(message);
    }

    public void add(AbstractPythonScope scope) {
        scopes.add(scope);
    }

    private void expandScopeImports(AbstractPythonScope scope) {

        Map<String, String> imports = scope.getImports();
        Collection<Map.Entry<String, String>> importEntries = imports.entrySet();

        log("Expanding: " + scope.toString());

        for (Map.Entry<String, String> entry : importEntries) {
            String alias = entry.getKey();
            String importPath = entry.getValue();
            if (importPath.startsWith(".")) {
                AbstractPythonScope baseScope = scope.findParent(PythonModule.class);
                while ((importPath = importPath.substring(1)).startsWith(".")) {
                    baseScope = baseScope.findParent(PythonModule.class);
                }

                String basePath = baseScope.getFullName();
                importPath = basePath + "." + importPath;
                imports.put(alias, importPath);
            }
        }

        log("Finished expanding: " + scope.toString());
    }

    /**
     * Expands relative import paths to their full path names.
     */
    public void expandImports() {
        LOG.info("Expanding scope imports");
        long start = System.currentTimeMillis();

        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                expandScopeImports(pyModule);
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                //expandScopeImports(pyClass);
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                //expandScopeImports(pyFunction);
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                //expandScopeImports(pyVariable);
            }
        });

        long duration = System.currentTimeMillis() - start;
        LOG.info("Expanding scope imports took " + duration + "ms");
    }

    public Collection<PythonModule> getModules() {
        final LinkedList<PythonModule> modules = new LinkedList<PythonModule>();
        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                modules.add(pyModule);
            }

            @Override public void visitClass(PythonClass pyClass) { }
            @Override public void visitFunction(PythonFunction pyFunction) { }
            @Override public void visitPublicVariable(PythonPublicVariable pyVariable) { }
        });
        return modules;
    }

    public Collection<PythonFunction> getFunctions() {
        final LinkedList<PythonFunction> functions = new LinkedList<PythonFunction>();
        traverse(new PythonVisitor() {
            @Override public void visitModule(PythonModule pyModule) { }
            @Override public void visitClass(PythonClass pyClass) { }

            @Override public void visitFunction(PythonFunction pyFunction) {
                functions.add(pyFunction);
            }

            @Override public void visitPublicVariable(PythonPublicVariable pyVariable) { }
        });
        return functions;
    }

    public Collection<PythonClass> getClasses() {
        final LinkedList<PythonClass> classes = new LinkedList<PythonClass>();
        traverse(new PythonVisitor() {
            @Override public void visitModule(PythonModule pyModule) { }

            @Override public void visitClass(PythonClass pyClass) {
                classes.add(pyClass);
            }

            @Override public void visitFunction(PythonFunction pyFunction) { }
            @Override public void visitPublicVariable(PythonPublicVariable pyVariable) { }
        });
        return classes;
    }

    public Collection<PythonPublicVariable> getPublicVariables() {
        final LinkedList<PythonPublicVariable> variables = new LinkedList<PythonPublicVariable>();
        traverse(new PythonVisitor() {
            @Override public void visitModule(PythonModule pyModule) { }
            @Override public void visitClass(PythonClass pyClass) { }
            @Override public void visitFunction(PythonFunction pyFunction) { }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                variables.add(pyVariable);
            }
        });
        return variables;
    }

    public Collection<AbstractPythonScope> getAll() {
        final LinkedList<AbstractPythonScope> all = new LinkedList<AbstractPythonScope>();
        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                all.add(pyModule);
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                all.add(pyClass);
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                all.add(pyFunction);
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                all.add(pyVariable);
            }
        });
        return all;
    }

    public AbstractPythonScope findByFullName(@Nonnull String fullName) {
        AbstractPythonScope result = null;
        String firstPart = fullName.split("\\.")[0];
        String remainingPart = fullName.substring(fullName.indexOf(".") + 1);
        for (AbstractPythonScope child : scopes) {
            if (child.getName().equals(firstPart)) {
                result = findByPartialName(child, remainingPart);
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public <T extends AbstractPythonScope> T findByFullName(@Nonnull String fullName, Class<T> type) {
        AbstractPythonScope result = findByFullName(fullName);
        if (result != null && type.isAssignableFrom(result.getClass())) {
            return (T)result;
        } else {
            return null;
        }
    }

    public AbstractPythonScope findByPartialName(@Nonnull AbstractPythonScope base, @Nonnull String partialName) {
        if (partialName.length() == 0 || partialName.equals(base.getName())) {
            return base;
        }

        String currentPart;
        String nextPart;
        if (partialName.contains(".")) {
            currentPart = partialName.substring(0, partialName.indexOf("."));
            nextPart = partialName.substring(partialName.indexOf(".") + 1);
        } else {
            currentPart = partialName;
            nextPart = null;
        }


        for (AbstractPythonScope child : base.getChildScopes()) {
            if (child.getName().equals(currentPart)) {
                if (nextPart == null) {
                    return child;
                } else {
                    return findByPartialName(child, nextPart);
                }
            }
        }

        return null;
    }

    /**
     * @param fileName The name of the base file to begin searching through.
     * @return The set of AbstractPythonScopes contained within the file and its children (if it's a folder)
     */
    public Collection<AbstractPythonScope> findInFile(@Nonnull final String fileName) {
        final List<AbstractPythonScope> result = new LinkedList<AbstractPythonScope>();
        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                if (pyModule.getSourceCodePath().startsWith(fileName)) {
                    result.add(pyModule);
                }
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                if (pyClass.getSourceCodePath().startsWith(fileName)) {
                    result.add(pyClass);
                }
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                if (pyFunction.getSourceCodePath().startsWith(fileName)) {
                    result.add(pyFunction);
                }
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                if (pyVariable.getSourceCodePath().startsWith(fileName)) {
                    result.add(pyVariable);
                }
            }
        });
        return result;
    }

    /**
     * @param filePath The name of the specific file or directory to search for.
     * @return The module for the given folder or file.
     */
    public PythonModule findByFilePath(@Nonnull final String filePath) {
        //  TODO - Remove need for "container" collection
        final List<PythonModule> result = new LinkedList<PythonModule>();
        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                String sourcePath = pyModule.getSourceCodePath();
                if (sourcePath != null && sourcePath.equals(filePath)) {
                    result.add(pyModule);
                }
            }

            @Override
            public void visitClass(PythonClass pyClass) { }
            @Override
            public void visitFunction(PythonFunction pyFunction) { }
            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) { }
        });

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public <T extends AbstractPythonScope> T findFirstByFilePath(@Nonnull final String filePath, final Class<?> type) {
        final List<AbstractPythonScope> result = new ArrayList<AbstractPythonScope>();

        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                if (type == null) {
                    result.add(pyModule);
                    return;
                }
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonModule.class) &&
                        pyModule.getSourceCodePath().equals(filePath)) {
                    result.add(pyModule);
                }
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                if (type == null) {
                    result.add(pyClass);
                    return;
                }
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonClass.class) &&
                        pyClass.getSourceCodePath().equals(filePath)) {
                    result.add(pyClass);
                }
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                if (type == null) {
                    result.add(pyFunction);
                    return;
                }
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonFunction.class) &&
                        pyFunction.getSourceCodePath().equals(filePath)) {
                    result.add(pyFunction);
                }
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                if (type == null) {
                    result.add(pyVariable);
                    return;
                }
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonPublicVariable.class) &&
                        pyVariable.getSourceCodePath().equals(filePath)) {
                    result.add(pyVariable);
                }
            }
        });

        if (result.size() > 0) {
            return (T)result.get(0);
        } else {
            return null;
        }
    }


    /**
     * @param scope The module scope that imports will be relative to.
     * @param importRelativeToScope The import text, either in absolute form (a.b.package) or relative form (...b.package)
     * @return The set of Python scope objects matching the given import.
     */
    public Collection<AbstractPythonScope> resolveLocalImport(AbstractPythonScope scope, String importRelativeToScope) {
        if (!(scope instanceof PythonModule)) {
            scope = scope.findParent(PythonModule.class);
        }

        String basePath = "";
        if (importRelativeToScope.startsWith("..")) {
            int numParentTraversal = -1;
            for (int i = 0; i < importRelativeToScope.length(); i++) {
                if (importRelativeToScope.charAt(i) == '.') {
                    numParentTraversal++;
                } else {
                    break;
                }
            }

            AbstractPythonScope targetScope = scope;
            for (int i = 0; i < numParentTraversal; i++) {
                targetScope = targetScope.getParentScope();
            }

            basePath = targetScope.getFullName() + "." + importRelativeToScope.substring(numParentTraversal);

        } else if (importRelativeToScope.startsWith(".")) {
            String currentName = scope.getFullName();
            basePath = currentName + "." + importRelativeToScope.substring(1);
        }

        boolean wildcard = false;
        if (basePath.endsWith("*")) {
            wildcard = true;
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        if (basePath.endsWith(".")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        Collection<AbstractPythonScope> result = list();

        AbstractPythonScope resolvedScope = findByFullName(basePath);
        if (resolvedScope != null) {
            if (wildcard) {
                result.addAll(resolvedScope.getChildScopes());
            } else {
                result.add(resolvedScope);
            }
        }

        if (resolvedScope == null) {
            return null;
        } else {
            return result;
        }
    }



    public void traverse(PythonVisitor visitor) {
        for (AbstractPythonScope child : scopes) {
            Class<?> type = child.getClass();
            if (PythonClass.class.isAssignableFrom(type)) {
                visitor.visitClass((PythonClass)child);
            } else if (PythonFunction.class.isAssignableFrom(type)) {
                visitor.visitFunction((PythonFunction)child);
            } else if (PythonModule.class.isAssignableFrom(type)) {
                visitor.visitModule((PythonModule)child);
            } else if (PythonPublicVariable.class.isAssignableFrom(type)) {
                visitor.visitPublicVariable((PythonPublicVariable)child);
            }
            child.accept(visitor);
        }
    }

}
