package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonCodeCollection {

    private static SanitizedLogger LOG = new SanitizedLogger(PythonCodeCollection.class);

    List<AbstractPythonStatement> statements = list();

    private void log(String message) {
        LOG.debug(message);
        //LOG.info(message);
    }

    public void add(AbstractPythonStatement statement) {
        statements.add(statement);
    }

    private void expandStatementImports(AbstractPythonStatement statement) {

        Map<String, String> imports = statement.getImports();
        //  Work on copy to avoid concurrent modification
        Collection<Map.Entry<String, String>> importEntries = new HashMap<String, String>(imports).entrySet();

        for (Map.Entry<String, String> entry : importEntries) {
            String alias = entry.getKey();
            String multiImportPath = entry.getValue();
            String[] importPaths = multiImportPath.split("\\|");
            for (String importPath : importPaths) {
                if (importPath.startsWith(".")) {
                    AbstractPythonStatement baseScope = statement.findParent(PythonModule.class);
                    while ((importPath = importPath.substring(1)).startsWith(".")) {
                        baseScope = baseScope.findParent(PythonModule.class);
                    }

                    String basePath = baseScope.getFullName();
                    importPath = basePath + "." + importPath;
                }

                if (importPath.endsWith("*")) {

                    String basePath = importPath.substring(0, importPath.length() - 2);
                    AbstractPythonStatement baseScope;
                    if (basePath.length() == 0) {
                        baseScope = statement;
                    } else {
                        baseScope = findByFullName(basePath);
                    }

                    if (baseScope == null) {
                        log("Unable to expand wildcard import with unknown Python statement '" + basePath + "'");
                        continue;
                    }

                    for (AbstractPythonStatement child : baseScope.getChildStatements()) {
                        String name = child.getName();
                        imports.put(name, child.getFullName());
                    }

                    log("Expanded " + alias + " to " + baseScope.getChildStatements().size() + " statements");

                } else {
                    imports.put(alias, importPath);
                    log("Expanded " + alias + " to " + importPath);
                }
            }
        }

        log("Finished expanding: " + statement.toString());
    }

    public void initialize() {
        this.expandImports();
        this.collapseSymbolReferences();
        this.executeInvocations();
    }

    /**
     * Expands relative import paths to their full path names. Directory
     * modules with imports in their __init__.py have the imports added as direct
     * children to those modules.
     */
    public void expandImports() {
        LOG.info("Expanding statement imports");
        long start = System.currentTimeMillis();

        //  Expand local imports
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitAny(AbstractPythonStatement statement) {
                super.visitAny(statement);
                expandStatementImports(statement);
            }
        });

        //  Inherit imports from __init__.py when importing a module
        // ie cms.models.__init__.py imports .static and .dynamic; importing cms.models.*
        //  should also import cms.models.static and cms.models.dynamic
        //
        //  "Inheriting" is done by copying over each imported node and their full sub-nodes, should
        //  be resolving these needs when searching rather than doing a complete copy. Inefficient
        //  and interferes with variable value tracking
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                super.visitModule(pyModule);
                AbstractPythonStatement init = pyModule.findChild("__init__");

                if (init == null) {
                    return;
                }

                Map<String, String> imports = init.getImports();
                for (Map.Entry<String, String> entry : imports.entrySet()) {
                    //  Import path should be fully expanded
                    String alias = entry.getKey();
                    String fullPath = entry.getValue();
                    Collection<AbstractPythonStatement> importedStatements = resolveLocalImport(pyModule, fullPath);
                    if (importedStatements == null || importedStatements.size() == 0) {
                        continue;
                    } else {
                        for (AbstractPythonStatement child : importedStatements) {
                            AbstractPythonStatement clone = child.clone();

                            if (alias != null && !alias.endsWith("*") && importedStatements.size() == 0) {
                                clone.setName(alias);
                            }

                            pyModule.addChildStatement(clone);
                        }
                    }
                }
            }
        });

        long duration = System.currentTimeMillis() - start;
        LOG.info("Expanding statement imports took " + duration + "ms");
    }

    public void collapseSymbolReferences() {
        LOG.info("Collapsing symbol references");
        //  Various function and variable references have been made and captured. Neither
        //  have been resolved to their fully-qualified names yet.
        //
        //  1. Fully-qualify variables declared directly in a module.
        //  2. Fully-qualify variables referenced via 'self.ABC'.
        //  3. Remove function-local variable references that have been qualified outside of the function.
        //  4. Link variable modifications to their now-qualified references.
        //  5. Link function calls to their fully-qualified references.

        //  Lower-depth nodes are considered the definitions of a variable (unless defined in __init__)

        long startTime = System.currentTimeMillis();
        long duration;

        int numPrunedVariables = 0;
        Collection<PythonPublicVariable> variableDeclarations = this.getPublicVariables();
        for (PythonPublicVariable var : variableDeclarations) {
            String name = var.getName();
            if (name.contains(".") && !name.startsWith("self.")) {
                var.detach();
                ++numPrunedVariables;
            } else if (name.startsWith("self.")) {
                var.setName(name.substring(5));
            }
        }

        duration = System.currentTimeMillis() - startTime;
        LOG.info("Pruned " + numPrunedVariables + " redundant variable declarations in " + duration + "ms");
        startTime = System.currentTimeMillis();

        int numResolvedTypes = 0;
        variableDeclarations = this.getPublicVariables(); // use updated list of non-pruned variables
        for (PythonPublicVariable var : variableDeclarations) {
            String value = var.getValueString();
            if (value.contains("(")) {
                String methodName = value.substring(0, value.indexOf('('));
                PythonClass type = resolveLocalSymbol(methodName, var, PythonClass.class);
                if (type != null) {
                    var.setResolvedTypeClass(type);
                    Collection<PythonPublicVariable> members = type.getChildStatements(PythonPublicVariable.class);
                    for (PythonPublicVariable mem : members) {
                        var.addChildStatement(mem.clone());
                    }
                    ++numResolvedTypes;
                }
            }
        }

        duration = System.currentTimeMillis() - startTime;
        LOG.info("Resolved " + numResolvedTypes + " variable types in " + duration + "ms");

        int numResolvedModifications = 0;
        Collection<PythonFunction.PythonVariableModification> variableModifications = this.get(PythonFunction.PythonVariableModification.class);
        for (PythonFunction.PythonVariableModification var : variableModifications) {
            String varName = var.getTarget();
            PythonPublicVariable resolvedVar = resolveLocalSymbol(varName, var, PythonPublicVariable.class);
            if (resolvedVar != null) {
                var.setResolvedTarget(resolvedVar);
                ++numResolvedModifications;
            }
        }

        duration = System.currentTimeMillis() - startTime;
        LOG.info("Resolved " + numResolvedModifications + " variable modifications in " + duration + "ms");
        startTime = System.currentTimeMillis();

        int numResolvedFunctionCalls = 0;
        Collection<PythonFunction.PythonFunctionCall> functionCalls = this.get(PythonFunction.PythonFunctionCall.class);
        for (PythonFunction.PythonFunctionCall call : functionCalls) {
            String invokee = call.getInvokeeName();
            String function = call.getFunctionName();

            PythonPublicVariable resolvedInvokee = null;
            if (invokee != null) {
                resolvedInvokee = resolveLocalSymbol(invokee, call, PythonPublicVariable.class);
            }

            PythonFunction resolvedFunction = null;
            if (function != null) {
                if (resolvedInvokee != null) {
                    PythonClass invokeeType = resolvedInvokee.getResolvedTypeClass();
                    if (invokeeType != null) {
                        resolvedFunction = resolveLocalSymbol(function, invokeeType, PythonFunction.class);
                    }
                } else {
                    resolvedFunction = resolveLocalSymbol(function, call, PythonFunction.class);
                }
            } else {
                continue;
            }

            if (resolvedInvokee != null) {
                call.setResolvedInvokee(resolvedInvokee);
            }

            if (resolvedFunction != null) {
                call.setResolvedFunction(resolvedFunction);
                ++numResolvedFunctionCalls;
            }
        }

        duration = System.currentTimeMillis() - startTime;
        LOG.info("Resolved " + numResolvedFunctionCalls + " function and lambda calls in " + duration + "ms");

        LOG.info("Finished collapsing symbol references");
    }

    public void executeInvocations() {
        LOG.info("Executing global-level function calls");

        Collection<PythonFunction.PythonFunctionCall> functionCalls = new LinkedList<PythonFunction.PythonFunctionCall>();
        Collection<PythonModule> modules = getModules();

        for (PythonModule module : modules) {
            for (AbstractPythonStatement child : module.getChildStatements()) {
                if (child instanceof PythonFunction.PythonFunctionCall) {
                    functionCalls.add((PythonFunction.PythonFunctionCall)child);
                }
            }
        }

        long startTime = System.currentTimeMillis();

        for (PythonFunction.PythonFunctionCall call : functionCalls) {
            PythonFunction function = call.getResolvedFunction();
            if (function != null) {
                if (function.canInvoke()) {
                    Collection<String> params = call.getParameters();
                    String[] arrayParams;
                    if (params.size() == 0) {
                        arrayParams = new String[0];
                    } else {
                        arrayParams = params.toArray(new String[params.size()]);
                    }
                    function.invoke(this, call, call.getResolvedInvokee(), arrayParams);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        LOG.info("Executing " + functionCalls.size() + " function calls took " + duration + "ms");
    }

    public <T extends AbstractPythonStatement> Collection<T> get(final Class<T> type) {
        final LinkedList<T> result = new LinkedList<T>();
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitAny(AbstractPythonStatement statement) {
                if (type.isAssignableFrom(statement.getClass())) {
                    result.add((T)statement);
                }
            }
        });
        return result;
    }

    public Collection<PythonModule> getModules() {
        final LinkedList<PythonModule> modules = new LinkedList<PythonModule>();
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                modules.add(pyModule);
            }
        });
        return modules;
    }

    public Collection<PythonFunction> getFunctions() {
        final LinkedList<PythonFunction> functions = new LinkedList<PythonFunction>();
        traverse(new AbstractPythonVisitor() {
            @Override public void visitFunction(PythonFunction pyFunction) {
                functions.add(pyFunction);
            }
        });
        return functions;
    }

    public Collection<PythonClass> getClasses() {
        final LinkedList<PythonClass> classes = new LinkedList<PythonClass>();
        traverse(new AbstractPythonVisitor() {
            @Override public void visitClass(PythonClass pyClass) {
                classes.add(pyClass);
            }
        });
        return classes;
    }

    public Collection<PythonPublicVariable> getPublicVariables() {
        final LinkedList<PythonPublicVariable> variables = new LinkedList<PythonPublicVariable>();
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitPublicVariable(PythonPublicVariable pyVariable) {
                variables.add(pyVariable);
            }
        });
        return variables;
    }

    public Collection<AbstractPythonStatement> getAll() {
        final LinkedList<AbstractPythonStatement> all = new LinkedList<AbstractPythonStatement>();
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitAny(AbstractPythonStatement statement) {
                all.add(statement);
            }
        });
        return all;
    }

    public AbstractPythonStatement findByFullName(@Nonnull String fullName) {

        if (!fullName.contains(".")) {
            for (AbstractPythonStatement child : statements) {
                if (child.getName().equals(fullName)) {
                    return child;
                }
            }
            return null;
        }

        AbstractPythonStatement result = null;
        String firstPart = fullName.substring(0, fullName.indexOf('.'));
        String remainingPart = fullName.substring(fullName.indexOf('.') + 1);
        for (AbstractPythonStatement child : statements) {
            if (child.getName().equals(firstPart)) {
                result = findByPartialName(child, remainingPart);
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public <T extends AbstractPythonStatement> T findByFullName(@Nonnull String fullName, Class<T> type) {
        AbstractPythonStatement result = findByFullName(fullName);
        if (result != null && type.isAssignableFrom(result.getClass())) {
            return (T)result;
        } else {
            return null;
        }
    }

    public AbstractPythonStatement findByPartialName(@Nonnull AbstractPythonStatement base, @Nonnull String partialName) {
        if (partialName.length() == 0 || partialName.equals(base.getName())) {
            return base;
        }

        String currentPart;
        String nextPart;
        if (partialName.contains(".")) {
            currentPart = partialName.substring(0, partialName.indexOf('.'));
            nextPart = partialName.substring(partialName.indexOf('.') + 1);
        } else {
            currentPart = partialName;
            nextPart = null;
        }


        for (AbstractPythonStatement child : base.getChildStatements()) {
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
     * @param filePath The name of the specific file or directory to search for.
     * @return The module for the given folder or file.
     */
    public PythonModule findByFilePath(@Nonnull final String filePath) {
        //  TODO - Remove need for "container" collection
        final List<PythonModule> result = new LinkedList<PythonModule>();
        traverse(new AbstractPythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                String sourcePath = pyModule.getSourceCodePath();
                if (sourcePath != null && sourcePath.equals(filePath)) {
                    result.add(pyModule);
                }
            }
        });

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }


    /**
     * @param scope The module scope that imports will be relative to.
     * @param importRelativeToScope The import text, either in absolute form (a.b.package) or relative form (...b.package)
     * @return The set of Python scope objects matching the given import.
     */
    public Collection<AbstractPythonStatement> resolveLocalImport(AbstractPythonStatement scope, String importRelativeToScope) {
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

            AbstractPythonStatement targetScope = scope;
            for (int i = 0; i < numParentTraversal; i++) {
                targetScope = targetScope.getParentStatement();
            }

            basePath = targetScope.getFullName() + "." + importRelativeToScope.substring(numParentTraversal);

        } else if (importRelativeToScope.startsWith(".")) {
            String currentName = scope.getFullName();
            basePath = currentName + importRelativeToScope;
        } else {
            basePath = importRelativeToScope;
        }

        boolean wildcard = false;
        if (basePath.charAt(basePath.length() - 1) == '*') {
            wildcard = true;
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        if (basePath.charAt(basePath.length() - 1) == '.') {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        Collection<AbstractPythonStatement> result = list();

        AbstractPythonStatement resolvedScope = findByFullName(basePath);
        if (resolvedScope != null) {
            if (wildcard) {
                result.addAll(resolvedScope.getChildStatements());
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


    public AbstractPythonStatement resolveLocalSymbol(@Nonnull String symbol, @Nonnull AbstractPythonStatement localScope) {

        AbstractPythonStatement result;

        if (symbol.startsWith("self.")) {
            while (!(localScope instanceof PythonClass)) {
                localScope = localScope.getParentStatement();
                if (localScope == null) {
                    return null;
                }
            }
            PythonClass ownerClass = (PythonClass)localScope;
            symbol = symbol.substring(5);
            return findByFullName(ownerClass.getFullName() + "." + symbol);
        }

        boolean symbolIsEmbedded = symbol.contains(".");

        // Try to find the symbol as a direct child
        if (!symbolIsEmbedded) {
            result = localScope.findChild(symbol);
        } else {
            result = findByPartialName(localScope, symbol);
        }
        if (result != null) {
            return result;
        }

        // Try to find the symbol as an absolute reference
        result = findByFullName(symbol);
        if (result != null) {
            return result;
        }

        AbstractPythonStatement currentImportScope = localScope;
        Map<String, String> imports = currentImportScope.getImports();
        while (currentImportScope != null && imports.size() == 0) {
            imports = currentImportScope.getImports();
            currentImportScope = currentImportScope.getParentStatement();
            if (currentImportScope != null) {
                if (!symbolIsEmbedded) {
                    result = currentImportScope.findChild(symbol);
                } else {
                    result = findByPartialName(currentImportScope, symbol);
                }
                if (result != null) {
                    return result;
                }
            }
        }

        for (Map.Entry<String, String> entry : imports.entrySet()) {
            String alias = entry.getKey();
            String fullName = entry.getValue();

            if (symbol.startsWith(alias)) {
                result = findByFullName(symbol.replace(alias, fullName));
                if (result != null) {
                    break;
                }
            }
        }

        if (result != null) {
            return result;
        }

        // Couldn't resolve based on current scope, try in parent scopes
        AbstractPythonStatement currentScope = localScope;
        do {
            currentScope = currentScope.getParentStatement();
            if (currentScope == null) {
                break;
            }
            result = resolveLocalSymbol(symbol, currentScope);

        } while (!(currentScope instanceof PythonModule) && result == null);

        return result;
    }

    public <T extends AbstractPythonStatement> T resolveLocalSymbol(@Nonnull String symbol, @Nonnull AbstractPythonStatement localScope, @Nonnull Class<T> type) {
        AbstractPythonStatement statement = resolveLocalSymbol(symbol, localScope);
        if (statement != null && type.isAssignableFrom(statement.getClass())) {
            return (T)statement;
        } else {
            return null;
        }
    }



    public void traverse(AbstractPythonVisitor visitor) {
        for (AbstractPythonStatement child : statements) {
            child.accept(visitor);
        }
    }

}
