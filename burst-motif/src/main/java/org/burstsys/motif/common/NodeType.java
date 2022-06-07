/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import org.burstsys.motif.motif.tree.constant.context.*;
import org.burstsys.motif.motif.tree.data.context.*;
import org.burstsys.motif.motif.tree.eql.common.context.SchemaSourceContext;
import org.burstsys.motif.motif.tree.eql.common.context.StatementsContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.*;
import org.burstsys.motif.motif.tree.eql.queries.context.QueryContext;
import org.burstsys.motif.motif.tree.eql.queries.context.SelectContext;
import org.burstsys.motif.motif.tree.eql.queries.context.TargetContext;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentContext;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentDefinitionContext;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentSourceContext;
import org.burstsys.motif.motif.tree.logical.context.*;
import org.burstsys.motif.motif.tree.rule.context.EditFilterRuleContext;
import org.burstsys.motif.motif.tree.rule.context.PostsampleFilterRuleContext;
import org.burstsys.motif.motif.tree.rule.context.PresampleFilterRuleContext;
import org.burstsys.motif.motif.tree.values.context.*;
import org.burstsys.motif.motif.tree.view.ViewContext;
import org.burstsys.motif.schema.model.context.*;

import java.util.HashMap;

/**
 *
 */
public enum NodeType {

    //////////////////////////////////////////////////////////////
    // Paths
    //////////////////////////////////////////////////////////////
    PATH("PATH", PathAccessorContext.class),

    //////////////////////////////////////////////////////////////
    // Paths
    //////////////////////////////////////////////////////////////
    PARAMETER("PARAMETER", ParameterAccessorContext.class),

    //////////////////////////////////////////////////////////////
    // Constants
    //////////////////////////////////////////////////////////////
    BOOLEAN_CONSTANT("BOOL_C", BooleanConstantContext.class),
    BYTE_CONSTANT("BYTE_C", ByteConstantContext.class),
    SHORT_CONSTANT("SHORT_C", ShortConstantContext.class),
    INTEGER_CONSTANT("INTEGER_C", IntegerConstantContext.class),
    LONG_CONSTANT("LONG_C", LongConstantContext.class),
    DOUBLE_CONSTANT("DOUBLE_C", DoubleConstantContext.class),
    STRING_CONSTANT("STRING_C", StringConstantContext.class),
    NULL_CONSTANT("NULL_C", NullConstantContext.class),

    //////////////////////////////////////////////////////////////
    // Boolean Expressions
    //////////////////////////////////////////////////////////////
    VALUE_BOOLEAN("VAL_BE", BooleanValueExpressionContext.class),
    UNARY_BOOLEAN("UNARY_BE", UnaryBooleanExpressionContext.class),
    VALUE_COMPARE("VAL_COMP_BE", ValueComparisonBooleanExpressionContext.class),
    MEMBERSHIP_TEST_EXPLICIT("MEM_TST_EX_BE", ExplicitMembershipTestBooleanExpressionContext.class),
    MEMBERSHIP_TEST_VECTOR("MEM_TST_VEC_BE", VectorMembershipTestBooleanExpressionContext.class),
    NULL_TEST("NULL_TST_BE", NullTestBooleanExpressionContext.class),
    BOUNDS_TEST("BOUNDS_TST_BE", BoundsTestBooleanExpressionContext.class),
    BINARY_BOOLEAN("BINARY_BE", BinaryBooleanExpressionContext.class),

    //////////////////////////////////////////////////////////////
    // Value Expressions
    //////////////////////////////////////////////////////////////
    FUNCTION("FUNCTION", NowValueExpressionContext.class),
    NOW_VALUE("NOW_VE", NowValueExpressionContext.class),
    AGGREGATION_VALUE("AGG_VE", AggregationValueExpressionContext.class),
    TIME_VALUE("TIME_VAL_VE", DateTimeOrdinalExpressionContext.class),
    TIME_QUANTUM("TIME_QUA_VE", DateTimeQuantumExpressionContext.class),
    CAST("CAST_VE", CastValueExpressionContext.class),
    UNARY_VALUE("UNARY_VE", UnaryValueExpressionContext.class),
    BINARY_VALUE("BINARY_VE", BinaryValueExpressionContext.class),

    //////////////////////////////////////////////////////////////
    // Bindings
    //////////////////////////////////////////////////////////////
    VALUE_SCALAR_BINDING("VAL_SCAL_BD", ValueScalarBindingContext.class),
    VALUE_VECTOR_BINDING("VAL_VEC_BD", ValueVectorBindingContext.class),
    VALUE_MAP_BINDING("VAL_MAP_BD", ValueMapBindingContext.class),
    REFERENCE_SCALAR_BINDING("REF_SCAL_BD", ReferenceScalarBindingContext.class),
    REFERENCE_VECTOR_BINDING("REF_VEC_BD", ReferenceVectorBindingContext.class),
    INSTANCE_BINDING("INST_BD", InstanceBindingContext.class),
    TARGET_BINDING("TARGET_BD", TargetBindingContext.class),

    //////////////////////////////////////////////////////////////
    // Filters
    //////////////////////////////////////////////////////////////
    PRESAMPLE_RULE("PRESAMPLE_RL", PresampleFilterRuleContext.class),
    POSTSAMPLE_RULE("POSTSAMPLE_RL", PostsampleFilterRuleContext.class),
    EDIT_RULE("EDIT_RL", EditFilterRuleContext.class),
    VIEW("VIEW", ViewContext.class),

    //////////////////////////////////////////////////////////////
    // Queries
    //////////////////////////////////////////////////////////////
    QUERY("QUERY", QueryContext.class),
    SELECT("SELECT", SelectContext.class),
    TARGET("TARGET", TargetContext.class),
    SCHEMA_SOURCE("SCHEMA_SOURCE", SchemaSourceContext.class),

    //////////////////////////////////////////////////////////////
    // Segments
    //////////////////////////////////////////////////////////////
    SEGMENT("SEGMENT", SegmentContext.class),
    SEGMENT_DEFINITION("SEGMENT_DEINITION", SegmentDefinitionContext.class),
    SEGMENT_SOURCE("SEGMENT_SOURCE", SegmentSourceContext.class),

    //////////////////////////////////////////////////////////////
    // Funnels
    //////////////////////////////////////////////////////////////
    FUNNEL("FUNNEL", SegmentContext.class),
    FUNNEL_SOURCE("FUNNEL_SOURCE", FunnelSourceContext.class),
    TRIGGERED_STEP_DEFINITION("TRIGGERED_STEP_DEFINITION", TriggeredStepDefinitionContext.class),
    FUNNEL_DEFINITION_LIST("FUNNEL_DEFINITION_LIST", FunnelMatchDefinitionListContext.class),
    FUNNEL_BRACKET_LIST("FUNNEL_BRACKET_LIST", FunnelMatchDefinitionStepListContext.class),
    FUNNEL_DEFINITION_REPEAT("FUNNEL_DEFINITION_REPEAT", FunnelMatchDefinitionRepeatContext.class),
    FUNNEL_DEFINITION_STEPID("FUNNEL_DEFINITION_STEPID", FunnelMatchDefinitionStepIdContext.class),

    BOUNDARY_BOOLEAN("BOUNDARY_BOOLEAN", BoundsTestBooleanExpressionContext.class),
    //////////////////////////////////////////////////////////////
    // Statements
    //////////////////////////////////////////////////////////////
    STATEMENTS("STATEMENTS", StatementsContext.class),

    //////////////////////////////////////////////////////////////
    // Schema Model
    //////////////////////////////////////////////////////////////
    SCHEMA_MODEL("SCHEMA", MotifSchemaContext.class),
    SCHEMA_MODEL_STRUCTURE("STRUCT", SchemaStructureContext.class),
    SCHEMA_MODEL_VALUE_SCALAR("VAL_SCAL", SchemaValueScalarContext.class),
    SCHEMA_MODEL_VALUE_MAP("VAL_MAP", SchemaValueMapContext.class),
    SCHEMA_MODEL_VALUE_VECTOR("VALUE_VEC", SchemaValueVectorContext.class),
    SCHEMA_MODEL_REFERENCE_SCALAR("REF_SCAL", SchemaReferenceScalarContext.class),
    SCHEMA_MODEL_REFERENCE_VECTOR("REF_VEC", SchemaReferenceVectorContext.class),

    //////////////////////////////////////////////////////////////
    // Schema Parse
    //////////////////////////////////////////////////////////////
    SCHEMA_PARSE_DATA_TYPE("XXX", null),
    SCHEMA_PARSE_FIELD("XXX", null),
    SCHEMA_PARSE_IDENTIFER("XXX", null),
    SCHEMA_PARSE_NUMBER("XXX", null),
    SCHEMA_PARSE_CLASSIFIER("XXX", null),
    SCHEMA_PARSE_REFERENCE_SCALAR("XXX", null),
    SCHEMA_PARSE_REFERENCE_VECTOR("XXX", null),
    SCHEMA_PARSE_ROOT("XXX", null),
    SCHEMA_PARSE_TREE("XXX", null),
    SCHEMA_PARSE_STRUCTURE("XXX", null),
    SCHEMA_PARSE_VALUE_MAP("XXX", null),
    SCHEMA_PARSE_VALUE_SCALAR("XXX", null),
    SCHEMA_PARSE_VALUE_NODE("XXX", null),
    SCHEMA_PARSE_VALUE_VECTOR("XXX", null);

    //////////////////////////////////////////////////////////////
    // Key
    //////////////////////////////////////////////////////////////
    private final String key;

    public String getKey() {
        return key;
    }

    //////////////////////////////////////////////////////////////
    // Implementation
    //////////////////////////////////////////////////////////////
    private final Class<? extends NodeContext> clazz;

    public Class<? extends NodeContext> getClazz() {
        return clazz;
    }

    //////////////////////////////////////////////////////////////
    // Key Lookup
    //////////////////////////////////////////////////////////////

    public static HashMap<String, NodeType> keyMap = null;

    static NodeType byKey(String key) {
        if (keyMap == null) {
            keyMap = new HashMap<>();
            for (NodeType nt : values()) {
                keyMap.put(nt.key, nt);
            }
        }
        return keyMap.get(key);
    }

    //////////////////////////////////////////////////////////////
    // Constructor
    //////////////////////////////////////////////////////////////

    NodeType(String key, Class<? extends NodeContext> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    //////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////

    public String asString() {
        return this.toString();
    }
}
