/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols;

import org.burstsys.motif.Metadata;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.funnels.FunnelPathBase;
import org.burstsys.motif.paths.schemas.SchemaPathBase;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.paths.segments.SegmentPathBase;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.symbols.functions.*;

import java.util.*;

import static java.lang.String.format;

class PathSymbolsContext implements PathSymbols {
    /**
     * this inner class contenxtualizes names
     */
    private static class ContextName {
        public Definition.Context context;
        public String name;

        public ContextName(Definition.Context context, String name) {
            this.context = context;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ContextName contextName = (ContextName) o;
            return context == contextName.context &&
                    name.equals(contextName.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(context, name);
        }

        @Override
        public String toString() {
            return context + ":" + name;
        }
    }

    /**
     * This is the contextualized scope map
     */
    private static class ScopeContext extends HashMap<ContextName, Definition> implements Scope {
        ScopeContext() {
            super();
        }

    }

    // Private State
    private final Metadata catalog;
    private String schemaName = null;
    private final Deque<Scope> definitions = new LinkedList<>();

    PathSymbolsContext(Metadata catalog) {
        this.catalog = catalog;
        // read only global
        pushScope();
        addCommonFunctionDefinitions();
        addCommonMotifSchemaDefinitions();
        // root scope
        pushScope();
    }

    @Override
    /*
      Set the root path in the current scope
     */
    public void setCurrentRootPath(String name) {
        MotifSchema schema = lookupSchema(name);
        if (schema != null) {
            // Allow the schema to be referenced as a source either by its name or by its root entity
            addAlias(Definition.Context.SOURCE, schema.getRootFieldName(), schema);
            addAlias(Definition.Context.SOURCE, name, schema);
            this.schemaName = name;
        }
    }

    @Override
    public StructurePath currentRootPath() {
        Definition def = getDefinition(Definition.Context.SCHEMA, schemaName);
        assert(def != null);
        assert(def instanceof MotifSchema);
        return SchemaPathBase.rootPath((MotifSchema)def);
    }


    @Override
    public Path path(String path, String key) {
        List<String> components = Arrays.asList(path.split("\\."));
        String head = components.get(0);

        Definition def = getDefinition(Definition.Context.SOURCE, head);

        if (def instanceof MotifSchema)
            return SchemaPathBase.formPath((MotifSchema) def, path, key);
        else if (def instanceof Funnel)
            return FunnelPathBase.formPath((Funnel) def, path);
        else if (def instanceof Segment)
            return SegmentPathBase.formPath((Segment) def, path);

        /*
           a separate path for targets seemed like a good idea, but it is fraught with complexity that needs thought
        def = getDefinition(Definition.Context.TARGET, head);
        if (def instanceof Target)
            return TargetPath.formPath((Target) def);
        else
        */
            return null;

    }

    @Override
    public void pushScope() {
        definitions.push(new ScopeContext());
    }

    @Override
    public Scope popScope() {
        assert definitions.size() > 2;
        return definitions.pop();
    }

    @Override
    public void restoreScope(Scope scope) {
        definitions.push(scope);
    }

    @Override
    public MotifSchema lookupSchema(String name) {
        return catalog.getSchema(name);
    }

    @Override
    public void addCurrentScopeDefinition(Definition.Context context, Definition symbol) {
        assert definitions.element() instanceof ScopeContext;
        ScopeContext scope = (ScopeContext) definitions.element();
        if (scope.put(new ContextName(context, symbol.getName()), symbol) != null)
            throw new ParseException(
                    format("%s named '%s' has already been defined", context, symbol.getName()));
    }

    @Override
    public void addParentScopeDefinition(Definition.Context context, Definition symbol) {
        ScopeContext parentScope = null;
        if (definitions.size() == 1) {
            assert definitions.element() instanceof ScopeContext;
            parentScope = (ScopeContext) definitions.element();
        } else if (definitions.size() > 1) {
            assert definitions.element() instanceof ScopeContext;
            Scope currentScope = definitions.pop();
            parentScope = (ScopeContext) definitions.element();
            definitions.push(currentScope);
        }
        assert parentScope != null;
        if (parentScope.put(new ContextName(context, symbol.getName()), symbol) != null)
            throw new ParseException(
                    format("%s named '%s' has already been defined", context, symbol.getName()));
    }

    @Override
    public void addAlias(Definition.Context context, String alias, Definition symbol) {
        assert definitions.element() instanceof ScopeContext;
        ScopeContext scope = (ScopeContext) definitions.element();
        scope.put(new ContextName(context, alias), symbol);
    }

    @Override
    public Definition getDefinition(Definition.Context context, String name) {
        for (Scope level: definitions) {
            assert level instanceof ScopeContext;
            ScopeContext scope = (ScopeContext) level;
            Definition def = scope.getOrDefault(new ContextName(context, name), null);
            if (def != null)
                return def;
        }
        return null;
    }

    private void addCommonMotifSchemaDefinitions() {
        Set<String> names = catalog.allSchemaAliases();
        for (String name : names) {
            MotifSchema schema = lookupSchema(name);
            addAlias(Definition.Context.SCHEMA, name, schema);
            addAlias(Definition.Context.SCHEMA, name.toLowerCase(), schema);
            addAlias(Definition.Context.SCHEMA, schema.getSchemaName(), schema);
        }
    }

    private void addCommonFunctionDefinitions() {
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new SizeFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new DateTimeFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new FrequencyFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new SplitFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new EnumFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new LastPathIsCompleteFunction());
        addCurrentScopeDefinition(Definition.Context.FUNCTION, new LastPathStepTimeFunction());
    }

}
