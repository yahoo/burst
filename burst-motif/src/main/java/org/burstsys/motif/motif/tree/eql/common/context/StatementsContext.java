/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.JsonSerde;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.EvaluationContext;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class StatementsContext extends EvaluationContext implements Statements {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<Statement> statements;

    @SuppressWarnings("unused")
    public StatementsContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.STATEMENTS);
    }

    public StatementsContext(
            NodeGlobal global, NodeLocation location,
            List<Statement> statements) {
        super(global, location, NodeType.STATEMENTS);
        this.statements = statements;
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public Statements finalizeStatements(PathSymbols pathSymbols) {
        statements = statements.stream().map(s -> {
            pathSymbols.pushScope();
            Stack<Evaluation> stack = new Stack<>();
            stack.push(this);
            s.bind(pathSymbols, stack);
            s.validate(pathSymbols, pathSymbols.currentRootPath(), stack);
            stack.pop();
            s = (Statement) s.optimize(pathSymbols);
            pathSymbols.popScope();
            return s;
        }).collect(Collectors.toList());
        return this;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        for (Statement s: statements) {
            s.bind(pathSymbols, stack);
        }
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        for (Statement s : statements) {
            s.validate(pathSymbols, scope, stack);
        }
    }

    @Override
    public String generateMotif(int level) {
        return statements.stream().map(i -> i.generateMotif(level+1)).collect(Collectors.joining("\n"));
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        statements = statements.stream().map(s -> (Statement)s.optimize(pathSymbols)).collect(Collectors.toList());
        return this;
    }
}
