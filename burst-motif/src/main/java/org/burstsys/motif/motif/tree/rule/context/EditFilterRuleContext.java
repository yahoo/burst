/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.rule.context;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.JsonSerde;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.rule.EditFilterRule;
import org.burstsys.motif.motif.tree.rule.FilterRuleType;

public final class EditFilterRuleContext extends FilterRuleContext implements EditFilterRule {

    public EditFilterRuleContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.EDIT_RULE);
    }

    public EditFilterRuleContext(NodeGlobal global, NodeLocation location, PathAccessor targetPath,
                                 BooleanExpression whereExpression, FilterRuleType filterRuleType) {
        super(global, location, NodeType.EDIT_RULE, targetPath, whereExpression, filterRuleType);
        this.type = filterRuleType;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    @Override
    public PathAccessor getTarget() {
        return target;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(type);
        builder.append('\n');
        builder.append(target.explain(level + 1));
        builder.append(where.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("type", type)
                .add("targetPath", target)
                .add("whereExpression", where)
                .toString();
    }


    @Override
    public FilterRuleType getEditType() {
        return type;
    }


    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(type.generateMotif(level));
        builder.append(' ');
        builder.append(target.generateMotif(level));
        builder.append(' ');
        builder.append("WHERE");
        builder.append(' ');
        builder.append(where.generateMotif(level + 1));
        return builder.toString();
    }
}
