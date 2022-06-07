/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.parser.statement;

import org.antlr.v4.runtime.tree.ParseTree;
import org.burstsys.motif.MotifGrammarBaseVisitor;
import org.burstsys.motif.MotifGrammarParser;
import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.constant.context.DoubleConstantContext;
import org.burstsys.motif.motif.tree.constant.context.NullConstantContext;
import org.burstsys.motif.motif.tree.constant.context.StringConstantContext;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.data.context.ParameterAccessorContext;
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.eql.common.context.SchemaSourceContext;
import org.burstsys.motif.motif.tree.eql.common.context.StatementsContext;
import org.burstsys.motif.motif.tree.eql.funnels.BoundaryBooleanExpression;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinition;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionList;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionRepeat;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionStepId;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;
import org.burstsys.motif.motif.tree.eql.funnels.context.BoundaryBooleanExpressionContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelMatchDefinitionListContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelMatchDefinitionRepeatContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelMatchDefinitionStepIdContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelMatchDefinitionStepListContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelSourceContext;
import org.burstsys.motif.motif.tree.eql.funnels.context.TriggeredStepDefinitionContext;
import org.burstsys.motif.motif.tree.eql.queries.Select;
import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.motif.tree.eql.queries.context.QueryContext;
import org.burstsys.motif.motif.tree.eql.queries.context.SelectContext;
import org.burstsys.motif.motif.tree.eql.queries.context.TargetContext;
import org.burstsys.motif.motif.tree.eql.segments.SegmentDefinition;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentContext;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentDefinitionContext;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentSourceContext;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.motif.tree.expression.context.ParameterDefinitionContext;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanOperatorType;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.logical.BoundsTestOperatorType;
import org.burstsys.motif.motif.tree.logical.MembershipTestOperatorType;
import org.burstsys.motif.motif.tree.logical.NullTestOperatorType;
import org.burstsys.motif.motif.tree.logical.UnaryBooleanOperatorType;
import org.burstsys.motif.motif.tree.logical.context.BinaryBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.BooleanValueExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.BoundsTestBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.ExplicitMembershipTestBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.NullTestBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.UnaryBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.ValueComparisonBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.VectorMembershipTestBooleanExpressionContext;
import org.burstsys.motif.motif.tree.rule.FilterRule;
import org.burstsys.motif.motif.tree.rule.FilterRuleType;
import org.burstsys.motif.motif.tree.rule.PostsampleFilterRule;
import org.burstsys.motif.motif.tree.rule.PresampleFilterRule;
import org.burstsys.motif.motif.tree.rule.context.EditFilterRuleContext;
import org.burstsys.motif.motif.tree.rule.context.FilterRuleContext;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;
import org.burstsys.motif.motif.tree.values.DateTimeConversionOperatorType;
import org.burstsys.motif.motif.tree.values.DateTimeOrdinalOperatorType;
import org.burstsys.motif.motif.tree.values.DateTimeQuantumOperatorType;
import org.burstsys.motif.motif.tree.values.UnaryValueOperatorType;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.motif.tree.values.context.AggregationValueExpressionContext;
import org.burstsys.motif.motif.tree.values.context.BinaryValueExpressionContext;
import org.burstsys.motif.motif.tree.values.context.CastValueExpressionContext;
import org.burstsys.motif.motif.tree.values.context.DateTimeConversionExpressionContext;
import org.burstsys.motif.motif.tree.values.context.DateTimeOrdinalExpressionContext;
import org.burstsys.motif.motif.tree.values.context.DateTimeQuantumExpressionContext;
import org.burstsys.motif.motif.tree.values.context.FunctionExpressionContext;
import org.burstsys.motif.motif.tree.values.context.NowValueExpressionContext;
import org.burstsys.motif.motif.tree.values.context.UnaryValueExpressionContext;
import org.burstsys.motif.motif.tree.view.ViewContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.burstsys.motif.common.NodeLocation.getLocation;

/**
 * MotifBuilder translates the antlr AST into the AST defined by this module. This class is not
 * intended for external consupmtion and is not considered part of the public API for this module.
 */
public class MotifBuilder extends MotifGrammarBaseVisitor<Node> {

    @Nonnull public final NodeGlobal global;

    public MotifBuilder(@Nonnull final String defaultTimeZoneName) {
        global = new NodeGlobal(Objects.requireNonNull(defaultTimeZoneName));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // View
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitView(MotifGrammarParser.ViewContext ctx) {
        NodeLocation location = getLocation(ctx);
        ArrayList<FilterRule> rules = new ArrayList<>();
        String name = extractViewName(ctx, location);
        for (MotifGrammarParser.RuleClauseContext rule : ctx.filterClause().ruleClause()) {
            FilterRuleContext newRule = (FilterRuleContext)visit(rule);
            String newTarget = newRule.getTarget().fullPathAsString();
            if (newRule instanceof PresampleFilterRule) {
                // Only one presample rule, for all targets.
                ensureSinglePresampleRule(rules, newRule);
            } else if (newRule instanceof PostsampleFilterRule) {
                // Only one postsample rule, for all targets.
                ensureSinglePostsampleRule(rules, newRule);
            } else {
                // Only one include rule per target.
                ensureSingleRulePerTarget(rules, newRule, newTarget);
            }
            rules.add(newRule);
        }
        return new ViewContext(global, location, name, rules);
    }

    private String extractViewName(MotifGrammarParser.ViewContext ctx, NodeLocation location) {
        String name = ctx.name instanceof MotifGrammarParser.QuotedIdentifierAlternativeContext ?
                ctx.name.getText().substring(1, ctx.name.getText().length() - 1) :
                ctx.name.getText();
        if (name.length() == 0)
            throw new ParseException(location, "View requires a complete name");
        return name;
    }

    private void ensureSinglePresampleRule(ArrayList<FilterRule> rules, FilterRuleContext newRule) {
        if (rules.stream().anyMatch(PresampleFilterRule.class::isInstance)) {
            throw new ParseException(newRule.getLocation(), "Only one presample rule is allowed");
        }
    }

    private void ensureSinglePostsampleRule(ArrayList<FilterRule> rules, FilterRuleContext newRule) {
        if (rules.stream().anyMatch(PostsampleFilterRule.class::isInstance)) {
            throw new ParseException(newRule.getLocation(), "Only one postsample rule is allowed");
        }
    }

    private void ensureSingleRulePerTarget(ArrayList<FilterRule> rules, FilterRuleContext newRule, String newTarget) {
        if (rules.stream()
                 .filter(r -> !(r instanceof PresampleFilterRule) && !(r instanceof PostsampleFilterRule))
                 .map(FilterRule::getTarget)
                 .anyMatch(x -> x.fullPathAsString().equals(newTarget)))
            throw new ParseException(newRule.getLocation(),
                                     format("Only one include rule for the target path '%s' is allowed", newTarget));
    }

    @Override
    public Node visitEditRuleExpression(MotifGrammarParser.EditRuleExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        FilterRuleType editRuleType = FilterRuleType.INCLUDE;
        PathAccessorContext targetPath = (PathAccessorContext) visit(ctx.target);
        if (ctx.where == null) {
            return new EditFilterRuleContext(global, location, targetPath, new BooleanConstantContext(global, location, true), editRuleType);
        } else {
            BooleanExpression whereExpression = (BooleanExpression) visit(ctx.where);
            return new EditFilterRuleContext(global, location, targetPath, whereExpression, editRuleType);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Data
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitAddValueExpression(MotifGrammarParser.AddValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new BinaryValueExpressionContext(global, location, left, right, BinaryValueOperatorType.ADD);
    }

    @Override
    public Node visitSubtractValueExpression(MotifGrammarParser.SubtractValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new BinaryValueExpressionContext(global, location, left, right, BinaryValueOperatorType.SUBTRACT);
    }

    @Override
    public Node visitDivideValueExpression(MotifGrammarParser.DivideValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new BinaryValueExpressionContext(global, location, left, right, BinaryValueOperatorType.DIVIDE);
    }

    @Override
    public Node visitMultiplyValueExpression(MotifGrammarParser.MultiplyValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new BinaryValueExpressionContext(global, location, left, right, BinaryValueOperatorType.MULTIPLY);
    }

    @Override
    public Node visitModuloValueExpression(MotifGrammarParser.ModuloValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new BinaryValueExpressionContext(global, location, left, right, BinaryValueOperatorType.MODULO);
    }

    @Override
    public PathAccessorContext visitPath(MotifGrammarParser.PathContext ctx) {
        NodeLocation location = getLocation(ctx);
        ArrayList<String> components = new ArrayList<>();
        for (MotifGrammarParser.IdentifierContext i : ctx.identifier()) {
            components.add(i.getText());
        }
        if (ctx.mapKey() != null) {
            ValueExpression mapKey = (ValueExpression) visit(ctx.mapKey().valueExpression());
            return new PathAccessorContext(global, location, components, mapKey);
        } else
            return new PathAccessorContext(global, location, components);
    }

    @Override
    public ParameterAccessorContext visitParameterAccessor(MotifGrammarParser.ParameterAccessorContext ctx) {
        NodeLocation location = getLocation(ctx);
        // add the reference to the symbol table
        return new ParameterAccessorContext(global, location, ctx.parameter().identifier().getText());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Value Expressions
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitParenthesizedValueExpression(MotifGrammarParser.ParenthesizedValueExpressionContext ctx) {
        MotifGrammarParser.ExpressionContext tree = ctx.child;
        return super.visit(tree);
    }

    @Override
    public Node visitNowValueExpression(MotifGrammarParser.NowValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new NowValueExpressionContext(global, location);
    }

    @Override
    public Node visitFunctionalExpression(MotifGrammarParser.FunctionalExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        String op = ctx.functionExpression().identifier().getText();
        ArrayList<ValueExpression> expressions = new ArrayList<>();
        for (MotifGrammarParser.ValueExpressionContext expression : ctx.functionExpression().valueExpressionList().valueExpression()) {
            expressions.add((ValueExpression) visit(expression));
        }
        return new FunctionExpressionContext(global, location, expressions, op);
    }

    @Override
    public Node visitBasicAggregateFunctionExpression(MotifGrammarParser.BasicAggregateFunctionExpressionContext ctx) {
        return new AggregationValueExpressionContext(this, ctx);
    }

    @Override
    public Node visitAggregateFunctionExpression(MotifGrammarParser.AggregateFunctionExpressionContext ctx) {
        return new AggregationValueExpressionContext(this, ctx);
    }

    @Override
    public Node visitEqlQuery(MotifGrammarParser.EqlQueryContext ctx) {
        NodeLocation location = getLocation(ctx);
        List<Select> selects = new ArrayList<>();
        List<ParameterDefinition> parameters = new ArrayList<>();

        // extract the optional global where
        BooleanExpression where = null;
        if (ctx.where != null) {
            Node t = visit(ctx.where) ;
            if (t instanceof BooleanExpression)
                where = (BooleanExpression)t;
            else if (t instanceof ValueExpression)
                // delay the checking of the value expression type until bind
                where = new BooleanValueExpressionContext(global, location, (ValueExpression)t);
            else
                throw new ParseException(location, "where clause must contain a boolean expression");
        }

        // check the sources
        List<Source> sources = parseSourceList(location, ctx.sourceList());

        // add selects
        int i = 0;
        for (MotifGrammarParser.EqlParallelQueryContext pc: ctx.eqlParallelQuery()) {
            // see if the select has parameters
            if (i == 0)
                parameters = parseRangeArguments(location, pc.rangeArguments());
            else if (!parseRangeArguments(location, pc.rangeArguments()).isEmpty()) {
                throw new ParseException(location, "only the first select may have parameters");
            }
            i++;
            String name = (pc.identifier() == null) ? "query_" + i : pc.identifier().getText();

           selects.add(parseSelect(location, name, pc.targetList(), pc.where, pc.limitDeclaration()));
        }

        // limit
        Integer limit = parseLimit(location, ctx.limitDeclaration());

        return new QueryContext(global, location, parameters, selects, sources, where, limit);
    }

    public List<Source> parseSourceList(NodeLocation location,
                                        List<MotifGrammarParser.SourceListContext> sourceList) {
        List<Source> sources = new ArrayList<>();
        // check the sources
        for (MotifGrammarParser.SourceListContext sourceItem: sourceList) {
            for (MotifGrammarParser.NamedSourceContext item : sourceItem.namedSource()) {
                String alias = null;
                if (item.AS() != null) {
                    alias = StringConstantContext.normalizeString(item.identifier().getText());
                }

                if (item.source().schemaSource() != null) {
                    MotifGrammarParser.SchemaSourceContext ssc = item.source().schemaSource();
                    sources.add(new SchemaSourceContext(global, location, StringConstantContext.normalizeString(ssc.identifier().getText()), alias));
                } else if (item.source().segmentSource() != null) {
                    MotifGrammarParser.SegmentSourceContext ssc = item.source().segmentSource();
                    List<ValueExpression> segmentParms = null;
                    if (ssc.parameters != null)
                        segmentParms = ssc.parameters.valueExpression().stream().
                                map(s -> (ValueExpression) visit(s)).collect(Collectors.toList());
                    sources.add(new SegmentSourceContext(global, location,
                            StringConstantContext.normalizeString(ssc.identifier().getText()),
                            alias, segmentParms));
                } else if (item.source().funnelSource() != null) {
                    MotifGrammarParser.FunnelSourceContext fsc = item.source().funnelSource();
                    List<ValueExpression> funnelParms = null;
                    if (fsc.parameters != null)
                        funnelParms = fsc.parameters.valueExpression().stream().
                                map(s -> (ValueExpression) visit(s)).collect(Collectors.toList());
                    sources.add(new FunnelSourceContext(global, location,
                            StringConstantContext.normalizeString(fsc.identifier().getText()), alias, funnelParms));
                } else
                    throw new ParseException(location, format("unsupported source type %s", item));
            }
        }
        return sources;
    }

    @Override
    public Node visitMotifFunnel(MotifGrammarParser.MotifFunnelContext ctx) {
        NodeLocation location = getLocation(ctx);

        if (ctx.name == null)
            throw new ParseException(location, "funnel definition must have a name");

        String name = StringConstantContext.normalizeString(ctx.name.getText());

        Funnel.Type funnelType;
        if (ctx.TRANSACTION() != null)
           funnelType = Funnel.Type.TRANSACTION ;
        else
            funnelType = Funnel.Type.CONVERSION ;

        List<StepDefinition> steps = ctx.stepDefinition().stream().map(s -> {
            Node sd = visit(s);
            return (StepDefinition)sd;
        }).collect(Collectors.toList());

        // check the sources
        List<Source> sources = parseSourceList(location, Collections.singletonList(ctx.sourceList()));

        if (ctx.funnelDefinition() == null)
            throw new ParseException(location, "funnel must have a match definition");
        FunnelMatchDefinition definition = (FunnelMatchDefinition) visit(ctx.funnelDefinition());

        // parameters
        List<ParameterDefinition> parameters = parseRangeArguments(location, ctx.rangeArguments());

        // within
        ValueExpression within = (ValueExpression)visit(ctx.within);

        // limit
        Integer limit = parseLimit(location, ctx.limitDeclaration());

        // tags
        ArrayList<String> tags = parseTags(ctx.tagList());

        return new FunnelContext(global, location, name, funnelType, parameters, sources, steps, definition, within, limit, tags);
    }

    @Override
    public Node visitRepeatFunnelDefinition(MotifGrammarParser.RepeatFunnelDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);
        FunnelMatchDefinition def = (FunnelMatchDefinition)visit(ctx.funnelDefinition());
        int min = 0;
        int max = 1;
        if (ctx.min != null && ctx.max != null) {
            if (!ctx.min.getText().equals("*")) min = Integer.parseInt(ctx.min.getText());

            if (ctx.max.getText().equals("*")) max = FunnelMatchDefinitionRepeat.UNLIMITED;
            else max = Integer.parseInt(ctx.max.getText());
        } else if (!ctx.ASTERISK().isEmpty()) {
            max = FunnelMatchDefinitionRepeat.UNLIMITED;
        } else if (ctx.PLUS() != null) {
            min = 1;
            max = FunnelMatchDefinitionRepeat.UNLIMITED;
        } else if (ctx.QUESTION() == null) {
            throw new ParseException(location, "unrecognized repeat structure in match definition");
        }

        return new FunnelMatchDefinitionRepeatContext(global, location, min, max, def);
    }

    @Override
    public Node visitAndListFunnelDefinition(MotifGrammarParser.AndListFunnelDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);
        List<FunnelMatchDefinition> defs =
                ctx.funnelDefinition().stream().flatMap(d-> {
                    FunnelMatchDefinition fmd = (FunnelMatchDefinition)visit(d);
                    if (fmd instanceof FunnelMatchDefinitionList && ((FunnelMatchDefinitionList)fmd).getOp() == FunnelMatchDefinitionList.Op.AND)
                        return ((FunnelMatchDefinitionList) fmd).getSteps().stream();
                    else
                        return Stream.of(fmd);
                }).collect(Collectors.toList());
        return new FunnelMatchDefinitionListContext(global, location, FunnelMatchDefinitionList.Op.AND, defs);
    }

    @Override
    public Node visitOrListFunnelDefinition(MotifGrammarParser.OrListFunnelDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);
        List<FunnelMatchDefinition> defs =
                ctx.funnelDefinition().stream().flatMap(d-> {
                    FunnelMatchDefinition fmd = (FunnelMatchDefinition)visit(d);
                    if (fmd instanceof FunnelMatchDefinitionList && ((FunnelMatchDefinitionList)fmd).getOp() == FunnelMatchDefinitionList.Op.OR)
                        return ((FunnelMatchDefinitionList) fmd).getSteps().stream();
                    else
                        return Stream.of(fmd);
                }).collect(Collectors.toList());
        return new FunnelMatchDefinitionListContext(global, location, FunnelMatchDefinitionList.Op.OR, defs);
    }

    @Override
    public Node visitStepIdFunnelDefinition(MotifGrammarParser.StepIdFunnelDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);
        long id = Long.parseLong(ctx.stepId().getText());
        return new FunnelMatchDefinitionStepIdContext(global, location, id);
    }

    @Override
    public Node visitBracketFunnelDefinition(MotifGrammarParser.BracketFunnelDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);
        boolean negative = ctx.negating() != null;
        List<FunnelMatchDefinitionStepId> defs =
                ctx.stepId().stream().map(si-> {
                    NodeLocation siLocation = getLocation(si);
                    long id = Long.parseLong(si.getText());
                    return new FunnelMatchDefinitionStepIdContext(global, siLocation, id);
                }).collect(Collectors.toList());
        return new FunnelMatchDefinitionStepListContext(global, location, negative, defs);
    }

    @Override
    public Node visitParensFunnelDefinition(MotifGrammarParser.ParensFunnelDefinitionContext ctx) {
        Node n =  visit(ctx.funnelDefinition());
        if (ctx.nonCapture() != null)
            ((FunnelMatchDefinition)n).setNonCapture();
        return n;
    }

    @Override
    public Node visitStepDefinition(MotifGrammarParser.StepDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);

        long id = Long.parseLong(ctx.id.getText());

        if (ctx.when == null)
            throw new ParseException(location, "step definition must have a where clause");

        ValueExpression timing = (ValueExpression)visit(ctx.timing);
        ValueExpression within = (ValueExpression)visit(ctx.within);
        ValueExpression after = (ValueExpression)visit(ctx.after);
        BooleanExpression when = (BooleanExpression)visit(ctx.when) ;

        return new TriggeredStepDefinitionContext(global, location, id, timing, within, after, when);
    }

    @Override
    public Node visitStepWhen(MotifGrammarParser.StepWhenContext ctx) {
        NodeLocation location = getLocation(ctx);

        if (ctx.expression() != null) {
            Node t = visit(ctx.expression()) ;
            if (t instanceof BooleanExpression)
                return t;
            else if (t instanceof ValueExpression && ((ValueExpression) t).getDtype() == DataType.BOOLEAN)
                return new BooleanValueExpressionContext(global, location, (ValueExpression)t);
            else
                throw new ParseException(location, "where clause must contain a boolean expression");
        } else  {
            Node t = visit(ctx.boundary);
            if (!(t instanceof PathAccessor)) {
                throw new ParseException(location, "boundary must be a valid relation path");
            }
            BoundaryBooleanExpression.Type type = ctx.START() != null ?
                    BoundaryBooleanExpression.Type.START : BoundaryBooleanExpression.Type.END;
            return new BoundaryBooleanExpressionContext(global, location, type, (PathAccessor)t);
        }
    }

    @Override
    public Node visitMotifSegment(MotifGrammarParser.MotifSegmentContext ctx) {
        NodeLocation location = getLocation(ctx);

        if (ctx.name == null)
            throw new ParseException(location, "segment definition must have a name");

        String name = StringConstantContext.normalizeString(ctx.name.getText());

        // parameters
        List<ParameterDefinition> parameters = parseRangeArguments(location, ctx.rangeArguments());

        List<SegmentDefinition> definitions = ctx.motifSegmentDefinition().stream().map(s -> {
            Node sd = visit(s);
            if (!(sd instanceof SegmentDefinition))
                throw new ParseException(location, "expected segment definition");
            return (SegmentDefinition)sd;
        }).collect(Collectors.toList());

        // check the sources
        List<Source> sources = parseSourceList(location, Collections.singletonList(ctx.sourceList()));

        return new SegmentContext(global, location, name, parameters, sources, definitions);
    }

    @Override
    public Node visitMotifSegmentDefinition(MotifGrammarParser.MotifSegmentDefinitionContext ctx) {
        NodeLocation location = getLocation(ctx);

        String name = StringConstantContext.normalizeString(ctx.name.getText());

        BooleanExpression where;
        if (ctx.where == null)
            throw new ParseException(location, "segment definition must have a where clause");

        Node t = visit(ctx.where) ;
        if (t instanceof BooleanExpression)
            where = (BooleanExpression)t;
        else if (t instanceof ValueExpression && ((ValueExpression) t).getDtype() == DataType.BOOLEAN)
            where = new BooleanValueExpressionContext(global, location, (ValueExpression)t);
        else
            throw new ParseException(location, "where clause must contain a boolean expression");

        return new SegmentDefinitionContext(global, location, name, where);
    }

    @Override
    public Node visitMotifStatements(MotifGrammarParser.MotifStatementsContext ctx) {
        NodeLocation location = getLocation(ctx);
        List<Statement> statements =
                ctx.motifStatement().stream().map(s -> (Statement)visit(s)).collect(Collectors.toList());
        return new StatementsContext(global, location, statements);
    }

    @Override
    public Node visitMotifStatement(MotifGrammarParser.MotifStatementContext ctx) {
        return visitChildren(ctx);
    }

    private Integer parseLimit(NodeLocation location, MotifGrammarParser.LimitDeclarationContext limitContext) {
        if (limitContext != null) {
            Node limitC =  visit(limitContext);
            if (limitC instanceof NumberConstant) {
                return ((NumberConstant)limitC).asInteger();
            } else
                throw new ParseException(location, format("invalid limit value %s", limitContext.getText()));
        }
        return null;
    }

    private ArrayList<String> parseTags(MotifGrammarParser.TagListContext tagList) {
        ArrayList<String> tags = new ArrayList<>();

        if (tagList == null)
            return tags;

        for (MotifGrammarParser.IdentifierContext ti: tagList.identifier()) {
            if (ti.getText() != null && !ti.getText().trim().isEmpty())
                tags.add(ti.getText().trim()) ;
        }

        return tags;
    }

    private ArrayList<ParameterDefinition> parseRangeArguments(
            NodeLocation location,
            MotifGrammarParser.RangeArgumentsContext rangeArguments)
    {
        ArrayList<ParameterDefinition> parameters = new ArrayList<>();

        if (rangeArguments == null)
            return parameters;

        // Extract parameters and wire them to the parameter references
        for (MotifGrammarParser.ValueDeclarationContext vd: rangeArguments.valueDeclaration()) {
            if (vd.valueScalarDecl() != null) {
                if (vd.valueScalarDecl().simpleValueDatatype() == null)
                    throw new ParseException(location, "only the simple data types are supported for scalar parameters");
                DataType dataType = DataType.parse(vd.valueScalarDecl().simpleValueDatatype().getText());
                parameters.add(new ParameterDefinitionContext(global, location, vd.valueScalarDecl().identifier().getText(),
                        dataType));
            } else if (vd.valueVectorDecl() != null) {
                if (vd.valueVectorDecl().simpleValueDatatype() == null)
                    throw new ParseException(location, "only the simple data types are supported for vector parameters");
                DataType dataType = DataType.parse(vd.valueVectorDecl().simpleValueDatatype().getText());
                // TODO mark this as a vector
                parameters.add(new ParameterDefinitionContext(global, location, vd.valueVectorDecl().identifier().getText(),
                        dataType));
            }
        }
        return parameters;
    }

    private SelectContext parseSelect(NodeLocation location, String name, MotifGrammarParser.TargetListContext targets,
                                      MotifGrammarParser.BooleanExpressionContext where,
                                      MotifGrammarParser.LimitDeclarationContext limitContext) {
        List<Target> ts = new ArrayList<>();
        int targetIndex=0;
        for ( MotifGrammarParser.TargetItemContext item : targets.targetItem()) {
            targetIndex++;
            String ident = item.identifier() != null ?
                    StringConstantContext.normalizeString(item.identifier().getText()) : "Column_" + targetIndex;
            Expression expr;
            if (item.aggregateTarget() != null) {
                expr =  new AggregationValueExpressionContext(this, item.aggregateTarget());
            }
            else if (item.dimensionTarget() != null) {
                // make a dimension
                expr = (Expression) visit(item.dimensionTarget());
            }
            else {
                // verbatim expression
                expr = (Expression) visit(item.expression());
            }

            ts.add(new TargetContext(global, location, ident, expr));
        }

        // add the global where
        BooleanExpression processedWhere = null;
        if (where != null) {
            Node t = visit(where) ;
            if (t instanceof BooleanExpression)
                processedWhere = (BooleanExpression)t;
            else if (t instanceof ValueExpression && ((ValueExpression) t).getDtype() == DataType.BOOLEAN)
                processedWhere = new BooleanValueExpressionContext(global, location, (ValueExpression)t);
            else
                throw new ParseException(location, "where clause must contain a boolean expression");

        }


        // limit
        Integer limit = parseLimit(location, limitContext);
        return new SelectContext(global, location, name, ts, processedWhere, limit);
    }

    @Override
    public Node visitSchemaSource(MotifGrammarParser.SchemaSourceContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new SchemaSourceContext(global, location, visit(ctx.identifier()).toString(), null);
    }

    @Override
    public Node visitDomainSource(MotifGrammarParser.DomainSourceContext ctx) {
        NodeLocation location = getLocation(ctx);
        throw new ParseException(location, "Domain source is not supported yet.");
    }

    @Override
    public Node visitViewSource(MotifGrammarParser.ViewSourceContext ctx) {
        NodeLocation location = getLocation(ctx);
        throw new ParseException(location, "View source is not supported yet.");
    }

    @Override
    public Node visitEqCompareBooleanExpression(MotifGrammarParser.EqCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.EQ);
    }

    @Override
    public Node visitGtCompareBooleanExpression(MotifGrammarParser.GtCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.GT);
    }

    @Override
    public Node visitNeqCompareBooleanExpression(MotifGrammarParser.NeqCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.NEQ);
    }

    @Override
    public Node visitLtCompareBooleanExpression(MotifGrammarParser.LtCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.LT);
    }

    @Override
    public Node visitLteCompareBooleanExpression(MotifGrammarParser.LteCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.LTE);
    }

    @Override
    public Node visitGteCompareBooleanExpression(MotifGrammarParser.GteCompareBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression right = (ValueExpression) visit(ctx.right);
        return new ValueComparisonBooleanExpressionContext(global, location, left, right, BinaryValueComparisonOperator.GTE);
    }

    @Override
    public Node visitCastValueExpression(MotifGrammarParser.CastValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        DataType datatype = DataType.parse(ctx.dataType().getText());
        return new CastValueExpressionContext(global, location, valueExpression, datatype);
    }

    @Override
    public Node visitUnaryMinusExpression(MotifGrammarParser.UnaryMinusExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        return new UnaryValueExpressionContext(global, location, valueExpression, UnaryValueOperatorType.NEGATE);
    }

    @Override
    public Node visitUnaryPlusValueExpression(MotifGrammarParser.UnaryPlusValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        // TODO could just return getExpr - does this help?
        return new UnaryValueExpressionContext(global, location, valueExpression, UnaryValueOperatorType.NORMAL);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Time Quantum
    /////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Node visitDatetimeConversionValueExpression(MotifGrammarParser.DatetimeConversionValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        DateTimeConversionOperatorType op = DateTimeConversionOperatorType.parse(global, location, ctx.datetimeConversionOperators().getText());
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        return new DateTimeConversionExpressionContext(global, location, valueExpression, op);
    }

    @Override
    public Node visitDatetimeQuantumValueExpression(MotifGrammarParser.DatetimeQuantumValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        DateTimeQuantumOperatorType op = DateTimeQuantumOperatorType.parse(location, ctx.datetimeQuantumOperators().getText());
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        ValueExpression zoneExpression;
        if (ctx.timeZoneArgument() != null)
            zoneExpression = (ValueExpression) visit(ctx.timeZoneArgument().valueExpression());
        else
            zoneExpression = new StringConstantContext(global, location, global.defaultTimeZoneName());
        return new DateTimeQuantumExpressionContext(global, location, valueExpression, op, zoneExpression);
    }

    @Override
    public Node visitDatetimeOrdinalValueExpression(MotifGrammarParser.DatetimeOrdinalValueExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        DateTimeOrdinalOperatorType op = DateTimeOrdinalOperatorType.parse(location, ctx.datetimeOrdinalOperators().getText());
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        ValueExpression zoneExpression;
        if (ctx.timeZoneArgument() != null)
            zoneExpression = (ValueExpression) visit(ctx.timeZoneArgument().valueExpression());
        else
            zoneExpression = new StringConstantContext(global, location, global.defaultTimeZoneName());
        return new DateTimeOrdinalExpressionContext(global, location, valueExpression, op, zoneExpression);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Boolean Expressions
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitNullTestBooleanExpression(MotifGrammarParser.NullTestBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        ValueExpression valueExpression = (ValueExpression) visit(ctx.valueExpression());
        NullTestOperatorType datatype = NullTestOperatorType.parse(ctx.nullTestOp().getText());
        return new NullTestBooleanExpressionContext(global, location, valueExpression, datatype);
    }

    @Override
    public Node visitExplicitMembershipTestBooleanExpression(MotifGrammarParser.ExplicitMembershipTestBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        MembershipTestOperatorType operator = MembershipTestOperatorType.parse(ctx.membershipTestOp().getText());
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ArrayList<ValueExpression> expressions = new ArrayList<>();
        for (MotifGrammarParser.ValueExpressionContext expression : ctx.valueExpressionList().valueExpression()) {
            expressions.add((ValueExpression) visit(expression));
        }
        return new ExplicitMembershipTestBooleanExpressionContext(global, location, left, expressions, operator);
    }

    @Override
    public Node visitVectorMembershipTestBooleanExpression(MotifGrammarParser.VectorMembershipTestBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        MembershipTestOperatorType operator = MembershipTestOperatorType.parse(ctx.membershipTestOp().getText());
        ValueExpression left = (ValueExpression) visit(ctx.left);
        PathAccessor path = (PathAccessor) visit(ctx.path());
        return new VectorMembershipTestBooleanExpressionContext(global, location, left, path, operator);
    }

    @Override
    public Node visitBoundsTestBooleanExpression(MotifGrammarParser.BoundsTestBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        BoundsTestOperatorType operator = BoundsTestOperatorType.parse(ctx.boundsTestOp().getText());
        ValueExpression left = (ValueExpression) visit(ctx.left);
        ValueExpression lower = (ValueExpression) visit(ctx.lower);
        ValueExpression upper = (ValueExpression) visit(ctx.upper);
        return new BoundsTestBooleanExpressionContext(global, location, left, lower, upper, operator);
    }

    @Override
    public Node visitUpwardValueExpression(MotifGrammarParser.UpwardValueExpressionContext ctx) {
        return super.visit(ctx.valueExpression());
    }

    @Override
    public Node visitBinaryBooleanExpression(MotifGrammarParser.BinaryBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        BooleanExpression left = convertToBooleanExpression(location, visit(ctx.booleanExpression(0)));
        BooleanExpression right = convertToBooleanExpression(location, visit(ctx.booleanExpression(1)));
        if (ctx.AND() != null) {
            return new BinaryBooleanExpressionContext(global, location, BinaryBooleanOperatorType.AND, left, right);
        } else {
            return new BinaryBooleanExpressionContext(global, location, BinaryBooleanOperatorType.OR, left, right);
        }
    }

    private BooleanExpression convertToBooleanExpression(NodeLocation location, Node n) {
        if (n instanceof BooleanExpression)
            return (BooleanExpression) n;
        else if (n instanceof ValueExpression) {
            ValueExpression raw = (ValueExpression) n;
            return new BooleanValueExpressionContext(global, location, raw);
        } else {
            throw new ParseException(location, "expression must contain a boolean expression");
        }
    }

    @Override
    public Node visitUnaryBooleanExpression(MotifGrammarParser.UnaryBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        UnaryBooleanOperatorType operator = UnaryBooleanOperatorType.parse(ctx.NOT().getText());
        BooleanExpression exp = convertToBooleanExpression(location, visit(ctx.subexpression));
        return new UnaryBooleanExpressionContext(global, location, operator, exp);
    }

    /*
    @Override
    public Node visitLiteralBooleanExpression(MotifGrammarParser.LiteralBooleanExpressionContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new BooleanConstantContext(global, location, ctx.BOOLEAN_LITERAL().getText());
    }
    */

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // literals
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitConstantValueExpression(MotifGrammarParser.ConstantValueExpressionContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public Node visitHexLiteral(MotifGrammarParser.HexLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        String value = ctx.HEX_LITERAL().getText();
        return DataType.findSmallestNumberConstant(global, location, value);
    }

    @Override
    public Node visitLongLiteral(MotifGrammarParser.LongLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        String value = ctx.LONG_LITERAL().getText();
        return DataType.findSmallestNumberConstant(global, location, value);
    }

    @Override
    public Node visitIntegerLiteral(MotifGrammarParser.IntegerLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        String value = ctx.INTEGER_LITERAL().getText();
        return DataType.findSmallestNumberConstant(global, location, value);
    }

    @Override
    public Node visitDoubleLiteral(MotifGrammarParser.DoubleLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new DoubleConstantContext(global, location, ctx.DOUBLE_LITERAL().getText());
    }

    @Override
    public Node visitStringLiteral(MotifGrammarParser.StringLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new StringConstantContext(global, location, ctx.STRING_LITERAL().getText());
    }

    @Override
    public Node visitNullLiteral(MotifGrammarParser.NullLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new NullConstantContext(global, location);
    }

    @Override
    public Node visitBooleanLiteral(MotifGrammarParser.BooleanLiteralContext ctx) {
        NodeLocation location = getLocation(ctx);
        return new BooleanConstantContext(global, location, ctx.BOOLEAN_LITERAL().getText());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Root
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visit(ParseTree tree) {
        if (tree != null)
            return tree.accept(this);
        else
            return null;
    }

}
