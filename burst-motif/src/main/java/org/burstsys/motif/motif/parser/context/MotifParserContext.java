/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.parser.context;

import org.burstsys.motif.Metadata;
import org.burstsys.motif.MotifGrammarBaseListener;
import org.burstsys.motif.MotifGrammarLexer;
import org.burstsys.motif.MotifGrammarParser;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.parser.statement.MotifBuilder;
import org.burstsys.motif.motif.parser.MotifParser;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.eql.queries.context.QueryContext;
import org.burstsys.motif.motif.tree.eql.common.context.SchemaSourceContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.symbols.PathSymbols;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Stack;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Deprecated
public class MotifParserContext implements MotifParser {

    private static final BaseErrorListener errorListener = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String message, RecognitionException e) {
            throw new ParseException(message, e, line, charPositionInLine);
        }
    };

    public Expression parseExpression(Metadata metadata, String schemaName, String expressionSource) {
        checkNotNull(schemaName, "schema was null");
        checkNotNull(expressionSource, "expressionSource was null");

        PathSymbols pathSymbols = PathSymbols.symbols(metadata);
        pathSymbols.setCurrentRootPath(schemaName);

        Expression expression = (Expression) invokeParser(expressionSource, MotifGrammarParser::expression, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());
        Stack<Evaluation> stack = new Stack<>();
        expression.bind(pathSymbols, stack);
        expression.validate(pathSymbols, pathSymbols.currentRootPath(), stack);
        expression = (Expression)expression.optimize(pathSymbols);
        return expression;
    }

    public View parseView(Metadata metadata, String schemaName, String viewSource) {
        return parseView(metadata, schemaName, viewSource, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());
    }

    public View parseView(Metadata metadata, String schemaName, String viewSource, String defaultTimeZoneName) {
        checkNotNull(schemaName, "schema was null");
        checkNotNull(viewSource, "viewSource was null");
        checkNotNull(defaultTimeZoneName, "defaultTimeZoneName was null");

        PathSymbols pathSymbols = PathSymbols.symbols(metadata);
        pathSymbols.setCurrentRootPath(schemaName);

        View view = (View) invokeParser(viewSource, MotifGrammarParser::view, defaultTimeZoneName);
        Stack<Evaluation> stack = new Stack<>();
        view.bind(pathSymbols, stack);
        view.validate(pathSymbols, pathSymbols.currentRootPath(), stack);
        view = (View)view.optimize(pathSymbols);
        return view;
    }

    public Query parseMotifQuery(Metadata metadata, String source) {
        checkNotNull(metadata, "metadata was null");
        checkNotNull(source, "source was null");
        QueryContext query = (QueryContext) invokeParser(source, MotifGrammarParser::eqlQuery, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());

        // validate the source
        // TODO add other sources
        if ( query.getSources().size() != 1 ||
                !(query.getSources().get(0) instanceof SchemaSourceContext))
            throw new ParseException(query.getLocation(), "query must have one schema source");

        PathSymbols pathSymbols = PathSymbols.symbols(metadata);
        pathSymbols.setCurrentRootPath(query.getSchemaName());

        Stack<Evaluation> stack = new Stack<>();
        query.bind(pathSymbols, stack);
        query.validate(pathSymbols, pathSymbols.currentRootPath(), stack);
        query = (QueryContext)query.optimize(pathSymbols);
        return query;
    }

    public Statements parseMotifStatements(Metadata metadata, String source) {
        checkNotNull(metadata, "metadata was null");
        checkNotNull(source, "source was null");
        Statements statements = (Statements) invokeParser(source, MotifGrammarParser::motifStatements,
                NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());

        // add interesting declarations to the parsingSymbols
        PathSymbols pathSymbols = PathSymbols.symbols(metadata);
        return statements.finalizeStatements(pathSymbols);
    }

    private Node invokeParser(String viewSpecification, Function<MotifGrammarParser, ParserRuleContext> parseFunction, String defaultTimeZoneName) {
        try {
            MotifGrammarLexer lexer = new MotifGrammarLexer(CharStreams.fromString(viewSpecification));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            MotifGrammarParser parser = new MotifGrammarParser(tokenStream);

            parser.addParseListener(new PostProcessor());

            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ParserRuleContext tree;
            try {
                // first, try parsing with potentially faster SLL mode
                parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
                tree = parseFunction.apply(parser);
            } catch (ParseCancellationException ex) {
                // if we fail, parseExpression with LL mode

                tokenStream.seek(0); // rewind input stream
                parser.reset();

                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                tree = parseFunction.apply(parser);
            }

            return new MotifBuilder(defaultTimeZoneName).visit(tree);
        } catch (StackOverflowError e) {
            throw new ParseException("too large (stack overflow while parsing)");
        }
    }

    private static class PostProcessor extends MotifGrammarBaseListener {
    }

}
