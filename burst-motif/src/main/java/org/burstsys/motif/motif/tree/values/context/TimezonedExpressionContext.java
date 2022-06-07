/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.constant.context.StringConstantContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

public abstract class TimezonedExpressionContext extends ExpressionContext {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression timezone;

    TimezonedExpressionContext(NodeGlobal global, NodeLocation location, NodeType ntype, ValueExpression timezone) {
        super(global, location, ntype);
        if (timezone == null)
            timezone = new StringConstantContext(global, location, global.defaultTimeZoneName());
        this.timezone = timezone;
    }

    TimezonedExpressionContext(NodeGlobal global, NodeType ntype, ValueExpression timezone) {
        super(global, ntype);
        if (timezone == null)
            timezone = new StringConstantContext(global, null, global.defaultTimeZoneName());
        this.timezone = timezone;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        timezone.validate(pathSymbols, scope, stack);
        stack.pop();
        if (timezone.getDtype().notNumeric() && timezone.getDtype() != DataType.STRING)
            throw new ParseException(getLocation(),
                    format("timezone expression '%s' must evaluate to a string or an integer", timezone.explain()));
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        timezone.bind(pathSymbols, stack);
        stack.pop();
    }


    @Override
    public Path getLowestEvaluationPoint() {
        return timezone.getLowestEvaluationPoint();
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            timezone = (ValueExpression) timezone.optimize(pathSymbols);
            if (timezone.canReduceToConstant()) {
                Constant c = timezone.reduceToConstant();
                if (c.isNumber()) {
                    int tzNumber = c.asInteger();
                    // convert this to a standard id
                    try {
                        DateTimeZone id = DateTimeZone.forOffsetHours(tzNumber);
                        timezone = new StringConstantContext(getGlobal(), ((NodeContext)timezone).getLocation(), id.getID());
                    } catch (Exception e) {
                        throw new ParseException(getLocation(),
                                format("timezone value '%s' is an invalid hour offset from UTC", timezone.explain()));
                    }
                } else if (c.isString()) {
                    String tzName = c.asString();
                    try {
                        // validate the timezone and normalize it
                        DateTimeZone id = DateTimeZone.forID(tzName);
                        timezone = new StringConstantContext(getGlobal(), ((NodeContext)timezone).getLocation(), id.getID());
                    } catch (Exception e) {
                        throw new ParseException(getLocation(),
                                format("timezone value '%s' is an invalid id", timezone.explain()));
                    }
                }
            }
            return this;
        }
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant zoneConstant = timezone.reduceToConstant();
        // we only do this on numbers
        if (!(zoneConstant instanceof NumberConstant))
            throw new ParseException(getLocation(), format("can't reduce expression %s", timezone.explain()));
        return zoneConstant;
    }

    @Override
    public Boolean canReduceToConstant() {
        return super.canReduceToConstant();
    }

    @Override
    public String generateMotif(int level) {
        return timezone.generateMotif(level);
    }

    @Override
    public String explain(int level) {
        return timezone.explain(level);
    }

    @Override
    public String toString() {
        return timezone.toString();
    }

    public ValueExpression getTimezone() {
        return timezone;
    }

    // parent interface returns the child
    @Override
    public List<Expression> getChildren() {
        return Collections.singletonList(timezone);
    }

    @Override
    public int childCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return timezone;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        if (index == 0) {
            old = timezone;
            timezone = (ValueExpression) value;
            return old;
        }
        throw new IndexOutOfBoundsException();
    }
}
