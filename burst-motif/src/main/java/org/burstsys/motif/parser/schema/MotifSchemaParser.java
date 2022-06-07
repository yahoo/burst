/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.parser.schema;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.burstsys.motif.MotifSchemaGrammarLexer;
import org.burstsys.motif.MotifSchemaGrammarParser;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.parser.BaseQuickParser;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.context.MotifSchemaContext;
import org.burstsys.motif.schema.tree.ParseSchema;

/**
 * A parser for motif schemas
 */
public interface MotifSchemaParser {
    /**
     * @return a new motif schema parser
     */
    public static MotifSchemaParser build() {
        return new MotifSchemaParserContext();
    }

    public MotifSchema parseSchema(String schemaSpecification);

    public MotifSchema parseSchema(String schemaSpecfication, String defaultTimeZoneName);

    class MotifSchemaParserContext extends BaseQuickParser<MotifSchemaGrammarLexer, MotifSchemaGrammarParser>
            implements MotifSchemaParser {

        @Override
        protected MotifSchemaGrammarLexer newLexer(String motif) {
            return new MotifSchemaGrammarLexer(CharStreams.fromString(motif));
        }

        @Override
        protected MotifSchemaGrammarParser newParser(CommonTokenStream tokens) {
            return new MotifSchemaGrammarParser(tokens);
        }

        @Override
        protected Node consumeAST(String defaultTimeZoneName, ParserRuleContext tree) {
            final NodeGlobal global = new NodeGlobal(defaultTimeZoneName);
            return new SchemaBuilder(global).visit(tree);
        }

        @Override
        public MotifSchema parseSchema(String schemaSpecification) {
            return parseSchema(schemaSpecification, NodeGlobal.defaultNodeGlobal().defaultTimeZoneName());
        }

        @Override
        public MotifSchema parseSchema(String schemaSpecfication, String defaultTimeZoneName) {
            ParseSchema tree = (ParseSchema) invokeParser(schemaSpecfication, MotifSchemaGrammarParser::schemaClause,
                                                          defaultTimeZoneName);
            return new MotifSchemaContext(tree);
        }
    }
}
