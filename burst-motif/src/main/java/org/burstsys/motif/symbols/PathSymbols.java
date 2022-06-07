/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols;

import org.burstsys.motif.Metadata;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.schema.model.MotifSchema;

import java.util.Map;

/**
 * The symbol table for path references
 */
public interface PathSymbols {

    /**
     * A abstract representation of a scope that can be saved and restored
     */
    interface Scope{}


    /**
     * Factory method for path symbols manager
     * @param catalog
     * @return
     */
    static PathSymbols symbols(Metadata catalog) {
        return new PathSymbolsContext(catalog);
    }

    /**
     * Set the current schema root path
     * @param name schema name
     */
    void setCurrentRootPath(String name);

    /**
     * Get the root path
     * @return path
     */
    Path currentRootPath();

    /**
     * Add a new scope.  All symbols added to this scope override lower scopes in the scope stack
     */
    void pushScope();

    /**
     * Remove and return the top scope.  This is ignored if only the root scope
     * exists
     */
    Scope popScope();

    /**
     * Restore a saved scope to the top of the scope level
     * @param scope
     */
    void restoreScope(Scope scope);

    /**
     * Add a new definition to the top scope.  The name is contextualized in order to allow
     * two symbols in two different contexts (eg.  function vs. target name) with the same string value
     * @param context definition context
     * @param symbol the new symbols
     * @throws org.burstsys.motif.common.ParseException if the symbols is already defined in the context
     */
    void addCurrentScopeDefinition(Definition.Context context, Definition symbol);

    /**
     * Add a new definition to the parent of the top scope which is useful when defining things that will span
     * motif statements.
     * The name is contextualized in order to allow
     * two symbols in two different contexts (eg.  function vs. target name) with the same string value.
     *
     * @param context definition context
     * @param symbol the new symbols
     * @throws org.burstsys.motif.common.ParseException if the symbols is already defined in the context
     */
    void addParentScopeDefinition(Definition.Context context, Definition symbol);

    void addAlias(Definition.Context context, String alias, Definition symbol);

    Definition getDefinition(Definition.Context context, String name);

    /**
     * Given a path and optional key, return a path object in the current root schema.
     * @param path path string
     * @param key optional map key if the path is to a map element
     * @return path for the string and key,  null if there is no matching path
     */
    Path path(String path, String key);

    /**
     * Find a named schema
     *
     * @param name of schema
     * @return schema object,  null if there is no match
     */
    MotifSchema lookupSchema(String name);
}
