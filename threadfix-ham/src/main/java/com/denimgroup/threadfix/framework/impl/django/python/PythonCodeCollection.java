package com.denimgroup.threadfix.framework.impl.django.python;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonCodeCollection {

    List<AbstractPythonScope> scopes = list();

    public void add(AbstractPythonScope scope) {
        scopes.add(scope);
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

        String currentPart = partialName.substring(0, partialName.indexOf("."));
        String nextPart = partialName.substring(partialName.indexOf(".") + 1);

        for (AbstractPythonScope child : base.getChildScopes()) {
            if (child.getName().equals(currentPart)) {
                return findByPartialName(child, nextPart);
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
     * @return The module for the given folder, or the set of AbstractPythonScopes contained within the given file.
     */
    public Collection<AbstractPythonScope> findByFilePath(@Nonnull final String filePath) {
        final List<AbstractPythonScope> result = new LinkedList<AbstractPythonScope>();
        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                if (pyModule.getSourceCodePath().equals(filePath)) {
                    result.add(pyModule);
                }
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                if (pyClass.getSourceCodePath().equals(filePath)) {
                    result.add(pyClass);
                }
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                if (pyFunction.getSourceCodePath().equals(filePath)) {
                    result.add(pyFunction);
                }
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                if (pyVariable.getSourceCodePath().equals(filePath)) {
                    result.add(pyVariable);
                }
            }
        });
        return result;
    }

    public <T extends AbstractPythonScope> T findFirstByFilePath(@Nonnull final String filePath, @Nonnull final Class<?> type) {
        final List<AbstractPythonScope> result = new ArrayList<AbstractPythonScope>();

        traverse(new PythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonModule.class) &&
                        pyModule.getSourceCodePath().equals(filePath)) {
                    result.add(pyModule);
                }
            }

            @Override
            public void visitClass(PythonClass pyClass) {
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonClass.class) &&
                        pyClass.getSourceCodePath().equals(filePath)) {
                    result.add(pyClass);
                }
            }

            @Override
            public void visitFunction(PythonFunction pyFunction) {
                if (result.size() == 0 &&
                        type.isAssignableFrom(PythonFunction.class) &&
                        pyFunction.getSourceCodePath().equals(filePath)) {
                    result.add(pyFunction);
                }
            }

            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
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
