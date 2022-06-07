/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.parser.schema;

import org.burstsys.motif.MotifSchemaGrammarBaseVisitor;
import org.burstsys.motif.MotifSchemaGrammarParser;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.schema.model.SchemaClassifierType;
import org.burstsys.motif.schema.tree.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SchemaBuilder translates the antlr AST for a Motif schema into the AST defined in this module. This class is not
 * intended for external consupmtion and is not considered part of the public API for this module.
 */
public class SchemaBuilder extends MotifSchemaGrammarBaseVisitor<Node> {

    @Nonnull
    private NodeGlobal global;

    public SchemaBuilder(NodeGlobal global) {
        this.global = Objects.requireNonNull(global);
    }

    @Override
    public Node visit(ParseTree tree) {
        return tree.accept(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Top Level Construct
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitSchemaClause(MotifSchemaGrammarParser.SchemaClauseContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx.identifier());
        String schemaName = ctx.identifier().getText();
        ParseRoot root = (ParseRoot) visit(ctx.rootClause());
        ArrayList<ParseStructure> structures = new ArrayList<>();
        for (MotifSchemaGrammarParser.StructureClauseContext structure : ctx.structureClause()) {
            ParseStructure node = (ParseStructure) visit(structure);
            structures.add(node);
        }
        return new ParseSchema(global, location, schemaName, root, structures);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // root construct
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitRootClause(MotifSchemaGrammarParser.RootClauseContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx.identifier(0));
        String rootFieldName = ctx.identifier(0).getText();
        String rootFieldType = ctx.identifier(1).getText();
        return new ParseRoot(global, location, rootFieldName, rootFieldType);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Structure
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Node visitStructureClause(MotifSchemaGrammarParser.StructureClauseContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx.identifier(0));
        String structureName = ctx.identifier(0).getText();
        if (ctx.identifier().size() > 1) {
            throw new ParseException(location, "Subclasses are not implimented for this version");
        }
        ArrayList<ParseRelation> fields = new ArrayList<>();
        for (MotifSchemaGrammarParser.RelationContext relation : ctx.relation()) {
            Node Expression = visit(relation);
            fields.add((ParseRelation) Expression);
        }
        return new ParseStructure(global, location, structureName, fields);
    }

    private ArrayList<ParseClassifier> extractClassifiers(List<MotifSchemaGrammarParser.ClassifierContext> classifiers) {
        ArrayList<ParseClassifier> fiers = new ArrayList<>();
        for (MotifSchemaGrammarParser.ClassifierContext classifier : classifiers) {
            ParseClassifier Node = (ParseClassifier) visit(classifier);
            fiers.add(Node);
        }
        return fiers;
    }

    @Override
    public Node visitClassifier(MotifSchemaGrammarParser.ClassifierContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseClassifier(global, location, SchemaClassifierType.parse(ctx.getText()));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // Relations
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Node visitRelation(MotifSchemaGrammarParser.RelationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Node visitValueMapRelation(MotifSchemaGrammarParser.ValueMapRelationContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseValueMap(global, location, Integer.parseInt(ctx.INTEGER_LITERAL().getText()),
                ctx.valueMapDecl().identifier().getText(), extractClassifiers(ctx.classifier()),
                visit(ctx.valueMapDecl().simpleValueDatatype(0)), visit(ctx.valueMapDecl().simpleValueDatatype(1)));
    }

    @Override
    public Node visitValueVectorRelation(MotifSchemaGrammarParser.ValueVectorRelationContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseValueVector(global, location, Integer.parseInt(ctx.INTEGER_LITERAL().getText()),
                ctx.valueVectorDecl().identifier().getText(), extractClassifiers(ctx.classifier()),
                visit(ctx.valueVectorDecl().simpleValueDatatype()));
    }

    @Override
    public Node visitValueScalarRelation(MotifSchemaGrammarParser.ValueScalarRelationContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseValueScalar(global, location, Integer.parseInt(ctx.INTEGER_LITERAL().getText()),
                ctx.valueScalarDecl().identifier().getText(), extractClassifiers(ctx.classifier()),
                visit(ctx.valueScalarDecl().simpleValueDatatype()));
    }

    @Override
    public Node visitReferenceScalarRelation(MotifSchemaGrammarParser.ReferenceScalarRelationContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseReferenceScalar(global, location, Integer.parseInt(ctx.INTEGER_LITERAL().getText()),
                ctx.referenceScalarDecl().identifier(0).getText(), extractClassifiers(ctx.classifier()),
                ctx.referenceScalarDecl().identifier(1).getText());
    }

    @Override
    public Node visitReferenceVectorRelation(MotifSchemaGrammarParser.ReferenceVectorRelationContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseReferenceVector(global, location, Integer.parseInt(ctx.INTEGER_LITERAL().getText()),
                ctx.referenceVectorDecl().identifier(0).getText(), extractClassifiers(ctx.classifier()),
                ctx.referenceVectorDecl().identifier(1).getText());
    }

    @Override
    public Node visitSimpleValueDatatype(MotifSchemaGrammarParser.SimpleValueDatatypeContext ctx) {
        NodeLocation location = NodeLocation.getLocation(ctx);
        return new ParseDataType(global, location, ctx.getText());
    }

    @Override
    public Node visitErrorNode(ErrorNode Node) {
        return null;
    }


}
