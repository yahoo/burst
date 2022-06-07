/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.data.SchemaBinding;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.funnels.FunnelPath;
import org.burstsys.motif.paths.funnels.FunnelPathBase;
import org.burstsys.motif.paths.funnels.FunnelPathsPath;
import org.burstsys.motif.paths.funnels.FunnelStepsPath;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.paths.segments.SegmentMembersFieldPath;
import org.burstsys.motif.paths.segments.SegmentMembersPath;
import org.burstsys.motif.paths.segments.SegmentPathBase;
import org.burstsys.motif.paths.targets.TargetPath;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

/**
 * a clause that captures a schema path reference
 */
@JsonRootName("Path")
@JsonTypeName("Path")
public final class PathAccessorContext extends ExpressionContext implements PathAccessor {

    @JsonProperty
    private String value;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty
    private ValueExpression mapKey;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BindingContext binding;

    @SuppressWarnings("unused")
    public PathAccessorContext() { super(NodeGlobal.defaultNodeGlobal(), NodeType.PATH); }

    public PathAccessorContext(NodeGlobal global, NodeLocation location, List<String> components) {
        this(global, location, fullPathAsString(components));
    }

    public PathAccessorContext(NodeGlobal global, NodeLocation location, List<String> components, ValueExpression mapKey) {
        this(global, location, fullPathAsString(components));
        this.mapKey = mapKey;
    }

    private PathAccessorContext(NodeGlobal global, NodeLocation location, String path) {
        super(global, location, NodeType.PATH);
        this.mapKey = null;
        this.value = path;
    }

    static public PathAccessorContext getPathAccessorContext(NodeGlobal global, NodeLocation location, PathSymbols pathSymbols, Path path) {
        PathAccessorContext pac = new PathAccessorContext(global, location, path.toString());
        pac.bind(pathSymbols, null);
        return pac;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        if (mapKey != null)
            mapKey.validate(pathSymbols, scope, stack);
    }

    @Override
    public DataType getDtype() {
        assert(binding != null);
        return binding.getDatatype();
    }

    @Override
    public Path getLowestEvaluationPoint() {
        if ((binding == null))
            throw new AssertionError();
        return binding.getEvalPath();
    }

    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        String key = null;
        if (mapKey != null) {
            key = mapKey.toString();
            mapKey.bind(pathSymbols, stack);
        }

        Path p = pathSymbols.path(fullPathAsString(), key);

        if (p instanceof RelationPath) {
            // some field in a structure
            RelationPath rp = (RelationPath)p;

            // make sure that a map key is only allowed in a map
            if (mapKey != null && rp.getRelationType() != RelationType.VALUE_MAP) {
                throw new ParseException(getLocation(),
                        format(" non map relation '%s' in structure '%s' in schema '%s' has map key '%s'",
                                rp.getFieldName(), rp.getStructureName(), rp.getSchema().getSchemaName(), mapKey)
                );
            }

            switch (rp.getRelationType()) {

                case REFERENCE_SCALAR:
                    // TODO is this necessary, nothing ever binds this way?
                    binding = new ReferenceScalarBindingContext(getGlobal(), getLocation(), rp);
                    break;
                case REFERENCE_VECTOR:
                    // TODO is this necessary, nothing ever binds this way?
                    binding = new ReferenceVectorBindingContext(getGlobal(), getLocation(), rp);
                    break;
                case VALUE_SCALAR:
                    binding = new ValueScalarBindingContext(getGlobal(), getLocation(), rp);
                    break;
                case VALUE_VECTOR:
                    binding = new ValueVectorBindingContext(getGlobal(), getLocation(), rp);
                    break;
                case VALUE_MAP:
                    binding = new ValueMapBindingContext(getGlobal(), getLocation(), rp, mapKey);
                    break;
                default:
                    throw new ParseException(getLocation(),
                            format("could not find relation '%s' in structure '%s' in schema '%s'",
                                    value, rp, rp.getSchema().getSchemaName())
                    );
            }
        } else if (p instanceof StructurePath) {
            // a reference to the structure object
            StructurePath sp = (StructurePath) p;
            if (mapKey != null) {
                throw new ParseException(getLocation(),
                        format("instance reference structure '%s' in schema '%s' has map key '%s'",
                                sp.getStructureName(), ((StructurePath) p).getSchema().getSchemaName(), mapKey)
                );
            }
            binding = new InstanceBindingContext(getGlobal(), getLocation(), sp);
        } else if (p instanceof FunnelPathBase) {
            if (p instanceof FunnelPathsPath)
                binding = new FunnelPathsBindingContext(getGlobal(), getLocation(), (FunnelPathsPath)p);
            else if (p instanceof FunnelStepsPath)
                binding = new FunnelStepsBindingContext(getGlobal(), getLocation(), (FunnelStepsPath)p);
            else if (p instanceof FunnelPath)
                binding = new FunnelBindingContext(getGlobal(), getLocation(), p, pathSymbols.currentRootPath());
            else
                throw new ParseException(getLocation(), format("could not bind path '%s' in funnel '%s'", fullPathAsString(),
                                ((FunnelPathBase)p).getFunnel().getName())
                );
        } else if (p instanceof SegmentPathBase) {
            if (p instanceof SegmentMembersPath)
                binding = new SegmentBindingContext(getGlobal(), getLocation(), (SegmentMembersPath) p);
            else
                throw new ParseException(getLocation(), format("could not bind path '%s' in segment '%s'", fullPathAsString(),
                        ((SegmentPathBase)p).getSegment().getName())
                );
        } else if (p instanceof TargetPath) {
            TargetPath tp = (TargetPath)p;
            binding = new TargetBindingContext(getGlobal(), getLocation(), p, tp.getTarget());
        } else {
            throw new ParseException(getLocation(), format("could not bind path '%s'", fullPathAsString()));
        }
    }

    @Override
    public String fullPathWithKeyAsString() {
        StringBuilder builder = new StringBuilder();
        builder.append(fullPathAsString());
        if (mapKey != null) {
            builder.append('[');
            builder.append('\'');
            builder.append(mapKey.reduceToConstant().getDataValue());
            builder.append('\'');
            builder.append(']');
        }
        return builder.toString();
    }

    @Override
    public String fullPathAsString() {
        return value;
    }

    private static String fullPathAsString(List<String> components) {
        if (components == null) return null;
        StringBuilder builder = new StringBuilder();
        for (String component : components) {
            builder.append(component);
            builder.append('.');
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\'');
        builder.append(fullPathAsString());
        builder.append('\'');
        builder.append('\n');
        builder.append(binding.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return  fullPathWithKeyAsString();
    }

    @Override
    public SchemaBinding getBinding() {
        return binding;
    }

    @Override
    public ValueExpression getMapKey() {
        return mapKey;
    }

    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    @Override
    public String generateMotif(int level) {
        return fullPathWithKeyAsString();
    }

    // parent only returns a child if there is a map key
    @Override
    public List<Expression> getChildren() {
        if (mapKey != null)
            return Collections.singletonList(mapKey);
        else
            return Collections.emptyList();

    }

    @Override
    public int childCount() {
        return (mapKey != null) ? 1 : 0;
    }

    @Override
    public Expression getChild(int index) {
        if (mapKey != null && index == 0)
            return mapKey;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        if (mapKey != null && index == 0) {
            Expression old = mapKey;
            mapKey = (ValueExpression)value;
            return old;
        }
        throw new IndexOutOfBoundsException();
    }
}
