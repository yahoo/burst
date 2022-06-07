/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.parser.statement;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.burstsys.motif.Metadata;
import org.burstsys.motif.MotifGrammarLexer;
import org.burstsys.motif.MotifGrammarParser;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.eql.common.context.SchemaSourceContext;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.eql.queries.context.QueryContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.parser.BaseQuickParser;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A parser for motif statements and clauses (e.g. views, expressions)
 */
public interface MotifStatementParser {

    /**
     * Create a new motif statement parser
     * @param metadata a metadata cache to use when validating the statements
     * @return a new parser
     */
    public static MotifStatementParser build(Metadata metadata) {
        return new MotifStatementParserContext(metadata);
    }

    /**
     * Parse a motif sub-expression
     * @param schemaName the schema to use when validating the expression
     * @param expressionSource the text to parse
     * @return the AST of the parsed expression
     */
    public Expression parseExpression(String schemaName, String expressionSource);

    /**
     * Parse a motif sub-expression
     * @param metadata ignored
     * @param schemaName the schema to use when validating the expression
     * @param expressionSource the text to parse
     * @return the AST of the parsed expression
     * @deprecated use {@link MotifStatementParser#parseExpression(String, String)} instead
     */
    @Deprecated
    public Expression parseExpression(Metadata metadata, String schemaName, String expressionSource);

    /**
     * Parse a motif view using the system default timezone.
     * @param schemaName the schema to use when validating the view
     * @param viewSource the text to parse
     * @return the AST of the parsed view
     */
    public View parseView(String schemaName, String viewSource);

    /**
     * Parse a motif view using the system default timezone.
     * @param metadata ignored
     * @param schemaName the schema to use when validating the view
     * @param viewSource the text to parse
     * @return the AST of the parsed view
     * @deprecated use {@link MotifStatementParser#parseView(String, String)} instead
     */
    @Deprecated
    public View parseView(Metadata metadata, String schemaName, String viewSource);

    /**
     * Parse a motif view using the specified timezone
     * @param schemaName the schema to use when validating the view
     * @param viewSource the text to parse
     * @param defaultTimeZoneName the name of the timezone to use
     * @return the AST of the parsed view
     */
    public View parseView(String schemaName, String viewSource, String defaultTimeZoneName);

    /**
     * Parse a motif view using the specified timezone
     * @param metadata ignored
     * @param schemaName the schema to use when validating the view
     * @param viewSource the text to parse
     * @param defaultTimeZoneName the name of the timezone to use
     * @return the AST of the parsed view
     * @deprecated use {@link MotifStatementParser#parseView(String, String, String)} instead
     */
    @Deprecated
    public View parseView(Metadata metadata, String schemaName, String viewSource, String defaultTimeZoneName);

    /**
     * Parse a motif query
     * @param source the text to parse
     * @return the AST of the parsed query
     */
    public Query parseMotifQuery(String source);

    /**
     * Parse a motif query
     * @param metadata ignored
     * @param source the text to parse
     * @return the AST of the parsed query
     * @deprecated use {@link MotifStatementParser#parseMotifQuery(String)} instead
     */
    @Deprecated
    public Query parseMotifQuery(Metadata metadata, String source);

    /**
     * Parse a series of motif statements
     * @param source the text to parse
     * @return the AST of the parsed statements
     */
    public Statements parseMotifStatements(String source);

    /**
     * Parse a series of motif statements
     * @param metadata ignored
     * @param source the text to parse
     * @return the AST of the parsed statements
     * @deprecated use {@link MotifStatementParser#parseMotifStatements(String)} instead
     */
    @Deprecated
    public Statements parseMotifStatements(Metadata metadata, String source);

    class MotifStatementParserContext extends BaseQuickParser<MotifGrammarLexer, MotifGrammarParser> implements
            MotifStatementParser {

        private final Metadata metadata;

        MotifStatementParserContext(Metadata metadata) {
            this.metadata = metadata;
        }

        protected MotifGrammarLexer newLexer(String motif) {
            return new MotifGrammarLexer(CharStreams.fromString(motif));
        }

        protected MotifGrammarParser newParser(CommonTokenStream tokens) {
            return new MotifGrammarParser(tokens);
        }

        protected Node consumeAST(String defaultTimeZoneName, ParserRuleContext tree) {
            return new MotifBuilder(defaultTimeZoneName).visit(tree);
        }

        public Expression parseExpression(String schemaName, String expressionSource) {
            requireSchemaAndSource(schemaName, expressionSource);

            Expression expression = (Expression) invokeParser(expressionSource, MotifGrammarParser::expression);
            return validateAndOptimize(expression, schemaName);
        }

        @Deprecated
        public Expression parseExpression(Metadata metadata, String schemaName, String expressionSource) {
            return parseExpression(schemaName, expressionSource);
        }

        public View parseView(String schemaName, String viewSource) {
            return parseView(schemaName, viewSource, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());
        }

        @Deprecated
        public View parseView(Metadata metadata, String schemaName, String viewSource) {
            return parseView(schemaName, viewSource);
        }

        public View parseView(String schemaName, String viewSource, String defaultTimeZoneName) {
            requireSchemaAndSource(schemaName, viewSource);
            checkNotNull(defaultTimeZoneName, "defaultTimeZoneName was null");

            View view = (View) invokeParser(viewSource, MotifGrammarParser::view, defaultTimeZoneName);
            return validateAndOptimize(view, schemaName);
        }

        @Deprecated
        public View parseView(Metadata metadata, String schemaName, String viewSource, String defaultTimeZoneName) {
            return parseView(schemaName, viewSource, defaultTimeZoneName);
        }

        public Query parseMotifQuery(String source) {
            checkNotNull(source, "source was null");

            QueryContext query = (QueryContext) invokeParser(source, MotifGrammarParser::eqlQuery);

            // TODO add other sources
            // validate the source
            if (!(query.getSources().size() == 1 && query.getSources().get(0) instanceof SchemaSourceContext))
                throw new ParseException(query.getLocation(), "query must have one schema source");

            return validateAndOptimize(query, query.getSchemaName());
        }

        @Deprecated
        public Query parseMotifQuery(Metadata metadata, String source) {
            return parseMotifQuery(source);
        }

        public Statements parseMotifStatements(String source) {
            checkNotNull(source, "source was null");
            Statements statements = (Statements) invokeParser(source, MotifGrammarParser::motifStatements);

            // add interesting declarations to the parsingSymbols
            PathSymbols pathSymbols = PathSymbols.symbols(metadata);
            return statements.finalizeStatements(pathSymbols);
        }

        @Deprecated
        public Statements parseMotifStatements(Metadata metadata, String source) {
            return parseMotifStatements(source);
        }

        private <T extends Evaluation> T validateAndOptimize(T evaluation, String schemaName) {
            PathSymbols pathSymbols = PathSymbols.symbols(metadata);
            pathSymbols.setCurrentRootPath(schemaName);
            Stack<Evaluation> stack = new Stack<>();
            evaluation.bind(pathSymbols, stack);
            evaluation.validate(pathSymbols, pathSymbols.currentRootPath(), stack);
            return (T) evaluation.optimize(pathSymbols);
        }

        private void requireSchemaAndSource(String schemaName, String motifSource) {
            checkNotNull(schemaName, "schema was null");
            checkNotNull(motifSource, "motif source was null");
        }

    }
}
