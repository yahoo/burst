/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.MotifGrammarParser;
import org.burstsys.motif.common.*;
import org.burstsys.motif.parser.statement.MotifBuilder;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.constant.context.ByteConstantContext;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.AggregationOperatorType;
import org.burstsys.motif.motif.tree.values.AggregationValueExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class AggregationValueExpressionContext extends ExpressionContext implements AggregationValueExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Expression expr;

    @JsonProperty
    private AggregationOperatorType op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Expression where;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Expression scope;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Expression quanta;

    @JsonProperty
    private Integer size;

    @SuppressWarnings("unused")
    public AggregationValueExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.AGGREGATION_VALUE);
    }

    @SuppressWarnings("unused")
    public AggregationValueExpressionContext(
            NodeGlobal global,
            NodeLocation location,
            Expression expr,
            AggregationOperatorType op,
            Expression where,
            Expression scope,
            Expression quanta,
            Integer size
    ) {
        super(global, location, NodeType.AGGREGATION_VALUE);
        this.op = op;
        this.expr = expr;
        this.where = where;
        this.scope = scope;
        this.quanta = quanta;
        this.size = size;
    }

    public AggregationValueExpressionContext(
            MotifBuilder builder,
            MotifGrammarParser.AggregateTargetContext ctx
    ) {
        super(builder.global, NodeLocation.getLocation(ctx), NodeType.AGGREGATION_VALUE);
        int size = 1;
        String text = "";
        if (ctx.aggregateTargetFunction().aggregateFunction() != null)  {
            text = ctx.aggregateTargetFunction().aggregateFunction().getText();
        } else if (ctx.aggregateTargetFunction().TOP() != null)  {
            text = ctx.aggregateTargetFunction().TOP().getText();
            if (ctx.aggregateTargetFunction().limit == null ||
                !(ctx.aggregateTargetFunction().limit instanceof MotifGrammarParser.IntegerLiteralContext))
                throw new ParseException(NodeLocation.getLocation(ctx), format("aggregate operator '%s' requires a integer size", text));
            size = Integer.parseInt(ctx.aggregateTargetFunction().limit.getText());
        }
        AggregationOperatorType operator = AggregationOperatorType.parse(text);
        if (operator == null)
            throw new ParseException(NodeLocation.getLocation(ctx), format("aggregate operator '%s' is unknown", text));

        ValueExpression expr = (ValueExpression) ctx.expression().accept(builder);
        this.op = operator;
        this.expr = expr;
        this.where = null;
        this.scope = null;
        this.quanta = null;
        this.size = size;
    }

    public AggregationValueExpressionContext(
            MotifBuilder builder,
            MotifGrammarParser.BasicAggregateFunctionExpressionContext ctx
    ) {
        super(builder.global, NodeLocation.getLocation(ctx), NodeType.AGGREGATION_VALUE);
        String text = ctx.aggregateFunction().getText();
        AggregationOperatorType operator = AggregationOperatorType.parse(text);
        if (operator == null)
            throw new ParseException(NodeLocation.getLocation(ctx), format("aggregate operator '%s' ", text));

        ValueExpression expr = (ValueExpression) ctx.target.accept(builder);

        this.op = operator;
        this.expr = expr;
        this.where = null;
        this.scope = null;
        this.quanta = null;
        this.size = 1;
    }

    public AggregationValueExpressionContext(
            MotifBuilder builder,
            MotifGrammarParser.AggregateFunctionExpressionContext ctx
    ) {
        super(builder.global, NodeLocation.getLocation(ctx), NodeType.AGGREGATION_VALUE);
        String text = ctx.aggregateFunction().getText();
        AggregationOperatorType operator = AggregationOperatorType.parse(text);
        if (operator == null)
                throw new ParseException(NodeLocation.getLocation(ctx), format("aggregate operator '%s' ", text));

        ValueExpression target = (ValueExpression) ctx.target.accept(builder);
        Expression where = ctx.where != null ? (Expression) ctx.where.accept(builder) : null;
        Expression scope = ctx.scope != null ? (Expression) ctx.scope.accept(builder) : null;
        Expression quanta = ctx.quanta != null ? (Expression) ctx.quanta.accept(builder) : null;

        this.op = operator;
        this.expr = target;
        this.where = where;
        this.scope = scope;
        this.quanta = quanta;
        this.size = 1;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        expr.bind(pathSymbols, stack);
        if (scope != null)
            scope.bind(pathSymbols, stack);
        if (quanta != null)
            quanta.bind(pathSymbols, stack);
        if (where != null)
            where.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        expr = (Expression)expr.optimize(pathSymbols);
        if (scope != null)
            scope=(Expression)scope.optimize(pathSymbols);
        if (quanta != null)
            quanta=(Expression)quanta.optimize(pathSymbols);
        if (where != null)
            where=(Expression)where.optimize(pathSymbols);
        return this;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        // validate the expression
        stack.push(this);
        expr.validate(pathSymbols, scope, stack);

        // fill in the scope as the parent if none was given
        if (this.scope == null) {
            this.scope = PathAccessorContext.getPathAccessorContext(getGlobal(), this.getLocation(), pathSymbols, scope);
            this.scope.bind(pathSymbols, stack);
        }

        // fill in the quanta if none was given
        if (this.quanta == null) {
            this.quanta = new ByteConstantContext(getGlobal(), this.getLocation(), (byte)1);
            this.quanta.bind(pathSymbols, stack);
        }

        // validate the function
        switch (op) {
            case UNIQUE:
            case COUNT:
                if (!(expr instanceof PathAccessor))
                    throw new ParseException(getLocation(),
                            format("aggregation '%s' expression '%s' must refer to a path", op, expr));
                PathAccessor path = (PathAccessor) expr;
                if (path.getBinding().getRelationType() != RelationType.INSTANCE &&
                    path.getBinding().getRelationType() != RelationType.VALUE_MAP &&
                    path.getBinding().getRelationType() != RelationType.VALUE_VECTOR
                )
                    throw new ParseException(getLocation(),
                            format("aggregation '%s' path '%s' wrong must refer to an instance", op, path));

                // Though we could allow it, it really doesn't make sense to do count(user.sessions) scope user.sessions.events
                if (expr.getLowestEvaluationPoint().higher(this.scope.getLowestEvaluationPoint()))
                    throw new ParseException(getLocation(),
                            format("scope expression '%s' of '%s(%s)' requires evaluation at or below the target '%s'", this.scope, op, expr, expr));
                break;
            case SUM:
            case MIN:
            case MAX:
                if (expr.getDtype().notNumeric())
                    throw new ParseException(getLocation(),
                            format("aggregation '%s' expression '%s' must operate on a numeric expression", op, expr));
                break;
            case TOP:
                if (size <= 0)
                    throw new ParseException(getLocation(),
                            format("aggregation '%s' size '%d' must greater than zero", op, size));
                break;
            default:
                throw new ParseException(getLocation(), format("aggregation '%s' not understood", op));
        }

        // validate the scope
        this.scope.validate(pathSymbols, scope, stack);
        quanta.validate(pathSymbols, scope, stack);
        if (quanta.getDtype().notNumeric() || quanta.getDtype().equals(DataType.DOUBLE))
            throw new ParseException(getLocation(),
                    format("aggregation '%s' quanta expression '%s' must be an integer", op, quanta));
        if (!quanta.canReduceToConstant())
            throw new ParseException(getLocation(),
                    format("aggregation '%s' quanta expression '%s' must evaluate to a constant", op, quanta));


        // the scope must be on path with the lowest evaluation point of the target
        //if (expr.getLowestEvaluationPoint().higher(this.scope.getLowestEvaluationPoint()))
        if (expr.getLowestEvaluationPoint().notOnPath(this.scope.getLowestEvaluationPoint()))
            throw new ParseException(getLocation(),
                    format("aggregation '%s' scope expression '%s' requires evaluation on path with the target '%s'", op, this.scope, expr));

        // validate the where clause
        if (where != null) {
            // validate the where but scope it to level of this aggregation
            where.validate(pathSymbols, expr.getLowestEvaluationPoint(), stack);
            // where.validate(parsingSymbols, this.scope.getLowestEvaluationPoint());
            // must be a boolean
            if (!where.getDtype().equals(DataType.BOOLEAN))
                throw new ParseException(getLocation(),
                        format("aggregation '%s' where expression '%s' must be a boolean", op, where));
            //
            if (expr.getLowestEvaluationPoint().notOnPath(where.getLowestEvaluationPoint()))
                throw new ParseException(getLocation(),
                        format("aggregation '%s' where expression '%s' cannot be evaluated on a compatible path with the target '%s'", op, where, expr));
        }
        stack.pop();
    }

    @Override
    public DataType getDtype() {
        switch (op) {
            case COUNT:
                return DataType.LONG;
            case SUM:
            case MIN:
            case MAX:
                if (expr.getDtype() == DataType.DOUBLE || expr.getDtype().notNumeric())
                    return expr.getDtype();
                else
                    return DataType.LONG;
            default:
                throw new ParseException(getLocation(), format("aggregation '%s' not understood", op));
        }
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return scope.getLowestEvaluationPoint();
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(op.generateMotif(level + 1));
        builder.append('(');
        builder.append(expr.generateMotif(level + 1));
        builder.append(')');
        if (scope != null) {
            builder.append(' ');
            builder.append(" SCOPE ");
            if (!hasDefaultQuanta()) {
                builder.append(' ');
                builder.append(" ROLLING ");
                builder.append(quanta.generateMotif(level + 1));
                builder.append(' ');
            }
            builder.append(scope.generateMotif(level + 1));
        }
        if (where != null) {
            builder.append(' ');
            builder.append(" WHERE ");
            builder.append(where.generateMotif(level + 1));
        }
        builder.append(") ");
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(expr.explain(level + 1));
        if (scope != null)
            builder.append(scope.explain(level + 1));
        if (quanta != null)
            builder.append(quanta.explain(level + 1));
        if (where != null)
            builder.append(where.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("expr", expr)
                .add("scope", scope)
                .add("quanta", quanta)
                .add("where", where)
                .toString();
    }

    @Override
    public Expression getExpr() {
        return expr;
    }

    @Override
    public AggregationOperatorType getOp() {
        return op;
    }

    @Override
    public Expression getWhere() { return where; }

    @Override
    public Expression getScope() { return scope; }

    @Override
    public boolean atRootScope() { return scope.getLowestEvaluationPoint().isRoot(); }

    @Override
    public Expression getQuanta() { return quanta; }

    @Override
    public boolean hasDefaultQuanta() { return quanta == null || (quanta.canReduceToConstant() && quanta.reduceToConstant().asLong() == 1); }

    @Override
    public Integer getSize() { return size; }

    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    // parent interface returns the children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(expr, where, scope, quanta);
    }

    @Override
    public int childCount() {
        return 4;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return expr;
            case 1: return where;
            case 2: return scope;
            case 3: return quanta;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        switch (index) {
            case 0:
                old = expr;
                expr = value;
                return old;
            case 1:
                old = where;
                where = value;
                return old;
            case 2:
                old = scope;
                scope = value;
                return old;
            case 3:
                old = quanta;
                quanta = value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

}
