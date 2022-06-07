/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif;

import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.parser.schema.MotifSchemaParser;
import org.burstsys.motif.parser.statement.MotifStatementParser;
import org.burstsys.motif.schema.model.MotifSchema;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The top-level point of interaction with motif views, schemas, and expressions. Most clients
 * should start with this class before using the more specalized interfaces {@link MotifStatementParser} or {@link MotifSchemaParser}.
 */
@SuppressWarnings("unused")
public interface Motif {

    /**
     * Used to check if a schema has been registered
     * @param name the name the schema was registered with
     * @return true if the schema is registered, false otherwise
     */
    boolean isSchemaRegistered(String name);

    /**
     * @param name the name of the schema to lookup
     * @return the schema, if one is registered with the given name, null otherwise
     */
    MotifSchema getSchema(String name);

    /**
     * Parse a motif schema specification. This will also register the parsed
     * schema for later use in {@link Motif#parseExpression(String, String)} or {@link Motif#parseView(String, String)}
     */
    MotifSchema parseSchema(String schemaSpecification);

    /**
     * explain (return an informative string representation of) a motif schema specification
     */
    String explainSchema(String schemaSpecification);

    /**
     * Parse a motif expression
     * @return an AST representing the expression
     * @deprecated use {@link Motif#parseExpression(String, String)} instead
     */
    @Deprecated
    Expression parseExpression(MotifSchema schema, String expressionSource);

    /**
     * Parse a motif expression
      @return an AST representing the expression
     */
    Expression parseExpression(String schemaName, String expressionSource);

    /**
     * Explain (print out informative string representation of output of parser) a motif expression specification
     * @deprecated use {@link Motif#explainExpression(String, String)} instead
     */
    @Deprecated
    String explainExpression(MotifSchema schema, String expressionSource);

    /**
     * Explain (print out informative string representation of output of parser) a motif expression specification
     */
    String explainExpression(String schemaName, String expressionSource);

    /**
     * Parses a Motif View specification.
     *
     * @param schema the data model
     * @param viewSpecification the view (query) specification
     * @return a syntax forest (the parsed view)
     * @deprecated use {@link Motif#parseView(String, String)} instead
     */
    @Deprecated
    View parseView(MotifSchema schema, String viewSpecification);

    /**
     * Parses a Motif View specification.
     *
     * @param schemaName the name of the data model
     * @param viewSpecification the view (query) specification
     * @return a syntax forest (the parsed view)
     */
    View parseView(String schemaName, String viewSpecification);

    /**
     * Parses a Motif View specification. The default time zone will be used to
     * evaluate all time zone-based expressions for which the query does not specify one. After parsing, the time
     * zone name is available from the View interface.
     *
     * @param schema the data model
     * @param viewSpecification the view (query) specification
     * @param defaultTimeZoneName identifies the time zone to use when the view spec does not specify
     * @return a syntax forest (the parsed view)
     * @deprecated use {@link Motif#parseView(String, String, String)} instead
     */
    @Deprecated
    View parseView(MotifSchema schema, String viewSpecification, String defaultTimeZoneName);

    /**
     * Parses a Motif View specification. The default time zone will be used to
     * evaluate all time zone-based expressions for which the query does not specify one. After parsing, the time
     * zone name is available from the View interface.
     *
     * @param schemaName the name of the data model
     * @param viewSpecification the view (query) specification
     * @param defaultTimeZoneName identifies the time zone to use when the view spec does not specify
     * @return a syntax forest (the parsed view)
     */
    View parseView(String schemaName, String viewSpecification, String defaultTimeZoneName);

    /**
     * Explain (print out informative representation of output of parser) a motif view specification
     * @deprecated use {@link Motif#explainView(String, String)} instead
     */
    @Deprecated
    String explainView(MotifSchema schema, String expressionSource);

    /**
     * Explain (print out informative representation of output of parser) a motif view specification
     */
    String explainView(String schema, String expressionSource);

    /**
     * Explain (print out informative representation of output of parser) a motif specification
     *
     */
    String explainMotif(String source);

    /**
     * Parse a motif query
     * @return an AST representing the query
     */
    Query parseMotifQuery(String source);

    /**
     * Parse a series of motif statements
     * @return a sytax forest of the statements
     */
    Statements parseMotifStatements(String source);

    static Motif build() {
        return new MotifContext();
    }

    class MotifContext implements Motif {
        private final Metadata metadata = Metadata.build();
        private final MotifSchemaParser schemaParser = MotifSchemaParser.build();
        private final MotifStatementParser motifParser = MotifStatementParser.build(metadata);

        @Override
        public boolean isSchemaRegistered(String name) {
            return metadata.getSchema(name) != null;
        }

        @Override
        public MotifSchema getSchema(String name) {
            return metadata.getSchema(name);
        }

        @Override
        public MotifSchema parseSchema(String schemaSpecification) {
            try {
                MotifSchema motifSchema = schemaParser.parseSchema(schemaSpecification);
                metadata.register(motifSchema);
                return motifSchema;
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Override
        public String explainSchema(String schemaSpecification) {
            return schemaParser.parseSchema(schemaSpecification).explain();
        }

        @Override
        public String explainExpression(String schemaName, String expressionSource) {
            return parseExpression(schemaName, expressionSource).explain();
        }

        @Deprecated
        @Override
        public String explainExpression(MotifSchema schema, String expressionSource) {
            return explainExpression(schema.getSchemaName(), expressionSource);
        }

        @Override
        public Expression parseExpression(String schemaName, String expressionSource) {
            try {
                return motifParser.parseExpression(schemaName, expressionSource);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Deprecated
        @Override
        public Expression parseExpression(MotifSchema schema, String expressionSource) {
            return parseExpression(schema.getSchemaName(), expressionSource);
        }

        @Override
        public View parseView(String schemaName, String viewSpecification) {
            try {
                return motifParser.parseView(schemaName, viewSpecification);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Deprecated
        @Override
        public View parseView(MotifSchema schema, String viewSpecification) {
            return parseView(schema.getSchemaName(), viewSpecification);
        }

        @Override
        public View parseView(String schemaName, String viewSpecification, String defaultTimeZoneName) {
            try {
                return motifParser.parseView(schemaName, viewSpecification, defaultTimeZoneName);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Deprecated
        @Override
        public View parseView(MotifSchema schema, String viewSpecification, String defaultTimeZoneName) {
            return parseView(schema.getSchemaName(), viewSpecification, defaultTimeZoneName);
        }

        @Override
        public String explainView(String schema, String viewSpecification) {
            return motifParser.parseView(schema, viewSpecification).explain();
        }

        @Override
        public String explainView(MotifSchema schema, String viewSpecification) {
            return explainView(schema.getSchemaName(), viewSpecification);
        }

        @Override
        public Query parseMotifQuery(String source) {
            try {
                return motifParser.parseMotifQuery(source);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Override
        public Statements parseMotifStatements(String source) {
            try {
                return motifParser.parseMotifStatements(source);
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                throw e;
            }
        }

        @Override
        public String explainMotif(String source) {
            return motifParser.parseMotifQuery(source).explain();
        }
    }

}
