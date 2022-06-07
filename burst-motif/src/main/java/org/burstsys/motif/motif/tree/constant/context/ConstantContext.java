/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public abstract class ConstantContext extends ExpressionContext implements Constant {

    private DataType dtype;

    private Path rootPath;

    @JsonProperty("value")
    private Object value;

    protected ConstantContext(NodeGlobal global, NodeLocation location, NodeType ntype, DataType dtype, String valueText) {
        super(global, location, ntype);
        this.dtype = dtype;
        value = dtype.coerce(valueText);
    }

    protected ConstantContext(NodeGlobal global, NodeLocation location, NodeType ntype, DataType dtype, Object value) {
        super(global, location, ntype);
        this.dtype = dtype;
        this.value = value;
    }

    protected ConstantContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public String generateMotif(int level) {
        return value.toString();
    }

    @Override
    public final Constant castTo(DataType dataType) {
        try {
            switch (dataType) {
                case BYTE:
                    return new ByteConstantContext(getGlobal(), getLocation(), this.asByte());
                case SHORT:
                    return new ShortConstantContext(getGlobal(), getLocation(), this.asShort());
                case INTEGER:
                    return new IntegerConstantContext(getGlobal(), getLocation(), this.asInteger());
                case LONG:
                    return new LongConstantContext(getGlobal(), getLocation(),  this.asLong());
                case DATETIME:
                    return new LongConstantContext(getGlobal(), getLocation(),  this.asDatetime());
                case DOUBLE:
                    return new DoubleConstantContext(getGlobal(), getLocation(), this.asDouble());
                case NULL:
                    this.asNull();
                    return new NullConstantContext(getGlobal(), getLocation());
                case STRING:
                    return new StringConstantContext(getGlobal(), getLocation(), this.asString());
                case BOOLEAN:
                    return new BooleanConstantContext(getGlobal(), getLocation(), this.asBoolean());
                default:
                    throw new ParseException(getLocation(), format("can't cast non number to  to %s", dataType));
            }
        } catch (NumberFormatException e) {
            throw new ParseException(getLocation(), format("can't cast to %s: %s", dataType,
                    e.getMessage()));
        }
    }


    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        this.rootPath = pathSymbols.currentRootPath();

    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        this.rootPath = pathSymbols.currentRootPath();
        return this;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("dtype", dtype)
                .add("value", value)
                .toString();
    }

    @Override
    public final Constant reduceToConstant() {
        return this;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(value);
        return endExplain(builder);
    }

    @JsonIgnore
    @Override
    public boolean isNumber() {
        return this instanceof NumberConstant;
    }

    @JsonIgnore
    @Override
    public boolean isBoolean() {
        return this.getDtype() == DataType.BOOLEAN;
    }

    @JsonIgnore
    @Override
    public boolean isNull() {
        return this.getDtype() == DataType.NULL;
    }

    @JsonIgnore
    @Override
    public boolean isString() {
        return this.getDtype() == DataType.STRING;
    }

    void checkConstantIsNumber(Constant constant) {
        if (!constant.isNumber())
            throw new ParseException(getLocation(), "not a constant");
    }

    void checkConstantIsString(Constant constant) {
        if (!constant.isString())
            throw new ParseException(getLocation(), "not a string");
    }

     void checkConstantIsBoolean(Constant constant) {
        if (!constant.isBoolean())
            throw new ParseException(getLocation(), "not a boolean");
    }

    @Override
    public DataType getDtype() {
        return dtype;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return this.rootPath;
    }

    @Override
    public Object getDataValue() {
        return value;
    }

    @Override
    public Boolean canReduceToConstant() {
        return true;
    }

    @Override
    public String asString() {
        throw new ParseException(getLocation(), "can't convert to string");
    }

    @Override
    public boolean asBoolean() {
        throw new ParseException(getLocation(), "can't convert to boolean");
    }

    @Override
    public void asNull() {
        throw new ParseException(getLocation(), "can't convert to null");
    }

    @Override
    public byte asByte() {
        throw new ParseException(getLocation(), "can't convert to byte");
    }

    @Override
    public short asShort() {
        throw new ParseException(getLocation(), "can't convert to short");
    }

    @Override
    public int asInteger() {
        throw new ParseException(getLocation(), "can't convert to integer");
    }

    @Override
    public long asLong() {
        throw new ParseException(getLocation(), "can't convert to long");
    }

    @Override
    public long asDatetime() {
        throw new ParseException(getLocation(), "can't convert to datetime");
    }

    @Override
    public double asDouble() {
        throw new ParseException(getLocation(), "can't convert to double");
    }

    // parent interface returns no children
    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int childCount() {
        return 0;
    }

    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        throw new IndexOutOfBoundsException();
    }
}
