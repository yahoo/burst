/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.ParseException;

import java.util.function.Function;

public abstract class BaseQuickParser<L extends Lexer, P extends Parser> {

    protected static final BaseErrorListener errorListener = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String message, RecognitionException e) {
            throw new ParseException(message, e, line, charPositionInLine);
        }
    };

    protected abstract L newLexer(String motif);

    protected abstract P newParser(CommonTokenStream tokens);

    protected abstract Node consumeAST(String defaultTimeZoneName, ParserRuleContext tree);

    protected Node invokeParser(String motif, Function<P, ParserRuleContext> parseFunction) {
        return invokeParser(motif, parseFunction, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());
    }

    protected Node invokeParser(String motif, Function<P, ParserRuleContext> parseFunction, String defaultTimeZoneName) {
        try {
            L lexer = newLexer(motif);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            P parser = newParser(tokenStream);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ParserRuleContext tree = parse(parseFunction, tokenStream, parser);

            return consumeAST(defaultTimeZoneName, tree);
        } catch (StackOverflowError e) {
            throw new ParseException("too large (stack overflow while parsing)");
        }
    }

    /**
     * First try parsing in SLL mode, if that fails then fall back to the slower–but more complete–LL mode.
     * @param parseFunction the part of the grammar to evaluate
     * @param tokenStream the input stream
     * @param parser the parser to use
     * @return an AST produced by parsing the input
     */
    private ParserRuleContext parse(Function<P, ParserRuleContext> parseFunction, CommonTokenStream tokenStream, P parser) {
        try {
            // first, try parsing with potentially faster SLL mode
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            return parseFunction.apply(parser);
        } catch (ParseCancellationException ex) {
            // if we fail, parseExpression with LL mode
            tokenStream.seek(0); // rewind input stream
            parser.reset();

            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            return parseFunction.apply(parser);
        }
    }
}
