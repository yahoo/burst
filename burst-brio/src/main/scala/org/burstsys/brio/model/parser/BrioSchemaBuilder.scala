/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.parser

import org.burstsys.brio.grammar.BrioSchemaGrammarParser.{ClassifierContext, SchemaClauseContext}
import org.burstsys.brio.grammar.{BrioSchemaGrammarBaseVisitor, BrioSchemaGrammarParser}
import org.burstsys.brio.model.parser.BrioSchemaParser._
import org.burstsys.brio.types.BrioTypes.{BrioSchemaName, BrioVersionKey}
import org.burstsys.vitals.errors.VitalsException

import scala.jdk.CollectionConverters._

/**
  *
  * @param tree
  */
final case
class BrioSchemaBuilder(tree: SchemaClauseContext) extends BrioSchemaGrammarBaseVisitor[BrioParseNode] {

  def build: BrioSchemaClause = visit(tree).asInstanceOf[BrioSchemaClause]

  override
  def visitSchemaClause(ctx: BrioSchemaGrammarParser.SchemaClauseContext): BrioParseNode = {
    new BrioSchemaClause {
      override val name: BrioSchemaName = ctx.Identifier.extract
      override val version: BrioVersionKey = ctx.versionClause.Number.getText.toInt
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val root: BrioSchemaRootClause = visit(ctx.rootClause).asInstanceOf[BrioSchemaRootClause]
      override val structures: Array[BrioSchemaStructureClause] = ctx.structureClause.asScala.map {
        s => visit(s).asInstanceOf[BrioSchemaStructureClause]
      }.toArray
    }
  }

  override
  def visitRootClause(ctx: BrioSchemaGrammarParser.RootClauseContext): BrioParseNode = {
    new BrioSchemaRootClause {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val rootFieldName: BrioSchemaIdentifier = ctx.Identifier(0).extract
      override val rootTypeName: BrioSchemaIdentifier = ctx.Identifier(1).extract
      var rootType: BrioSchemaStructureClause = _
    }
  }

  override
  def visitStructureClause(ctx: BrioSchemaGrammarParser.StructureClauseContext): BrioParseNode = {
    new BrioSchemaStructureClause {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val selfName: BrioSchemaIdentifier = ctx.Identifier(0).extract
      override val superTypeName: BrioSchemaIdentifier = if (ctx.Identifier.size > 1) ctx.Identifier(1).extract else null
      var superType: BrioSchemaStructureClause = _
      override val relations: Array[BrioSchemaRelationClause] = ctx.relation.asScala.map {
        r => visit(r).asInstanceOf[BrioSchemaRelationClause]
      }.toArray
      override var version: BrioVersionKey = _
    }
  }

  override
  def visitValueMapRelation(ctx: BrioSchemaGrammarParser.ValueMapRelationContext): BrioParseNode = {
    new BrioSchemaValueMapRelation {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val relationOrdinal: BrioSchemaOrdinal = ctx.Number.getText.toInt
      override val relationName: BrioSchemaIdentifier = ctx.Identifier.extract
      override val keyDatatype: BrioSchemaDataTypeClause = visit(ctx.valueDatatype(0)).asInstanceOf[BrioSchemaDataTypeClause]
      override val valueDatatype: BrioSchemaDataTypeClause = visit(ctx.valueDatatype(1)).asInstanceOf[BrioSchemaDataTypeClause]
      override val relationClassifiers: Array[BrioSchemaClassifierType] = extractClassifiers(ctx.classifier)
    }
  }

  override
  def visitValueVectorRelation(ctx: BrioSchemaGrammarParser.ValueVectorRelationContext): BrioParseNode = {
    new BrioSchemaValueVectorRelation {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val relationOrdinal: BrioSchemaOrdinal = ctx.Number.getText.toInt
      override val relationName: BrioSchemaIdentifier = ctx.Identifier.extract
      override val valueDatatype: BrioSchemaDataTypeClause = visit(ctx.valueDatatype).asInstanceOf[BrioSchemaDataTypeClause]
      override val relationClassifiers: Array[BrioSchemaClassifierType] = extractClassifiers(ctx.classifier)
    }
  }

  override
  def visitValueScalarRelation(ctx: BrioSchemaGrammarParser.ValueScalarRelationContext): BrioParseNode = {
    new BrioSchemaValueScalarRelation {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val relationOrdinal: BrioSchemaOrdinal = ctx.Number.getText.toInt
      override val relationName: BrioSchemaIdentifier = ctx.Identifier.extract
      override val valueDatatype: BrioSchemaDataTypeClause = visit(ctx.valueDatatype).asInstanceOf[BrioSchemaDataTypeClause]
      override val relationClassifiers: Array[BrioSchemaClassifierType] = extractClassifiers(ctx.classifier)
    }
  }

  override
  def visitReferenceScalarRelation(ctx: BrioSchemaGrammarParser.ReferenceScalarRelationContext): BrioParseNode = {
    new BrioSchemaReferenceScalarRelation {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val relationOrdinal: BrioSchemaOrdinal = ctx.Number.getText.toInt
      override val relationName: BrioSchemaIdentifier = ctx.Identifier(0).extract
      var referenceType: BrioSchemaStructureClause = _
      override val referenceTypeName: BrioSchemaIdentifier = ctx.Identifier(1).extract
      override val relationClassifiers: Array[BrioSchemaClassifierType] = extractClassifiers(ctx.classifier)
    }
  }

  override
  def visitReferenceVectorRelation(ctx: BrioSchemaGrammarParser.ReferenceVectorRelationContext): BrioParseNode = {
    new BrioSchemaReferenceVectorRelation {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val relationOrdinal: BrioSchemaOrdinal = ctx.Number.getText.toInt
      override val relationName: BrioSchemaIdentifier = ctx.Identifier(0).extract
      var referenceType: BrioSchemaStructureClause = _
      override val referenceTypeName: BrioSchemaIdentifier = ctx.Identifier(1).extract
      override val relationClassifiers: Array[BrioSchemaClassifierType] = extractClassifiers(ctx.classifier)
    }
  }

  override
  def visitClassifier(ctx: BrioSchemaGrammarParser.ClassifierContext): BrioParseNode = {
    ctx.getText match {
      case "key" => new BrioSchemaKeyClassifier {
        override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      }
      case "ordinal" => new BrioSchemaOrdinalClassifier {
        override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      }
      case _ => throw VitalsException(s"unknown classifier: ctx.getText")
    }
  }

  override
  def visitSimpleValueDatatype(ctx: BrioSchemaGrammarParser.SimpleValueDatatypeContext): BrioParseNode = {
    new BrioSchemaSimpleDataType {
      override val primitive: BrioSchemaPrimitive = ctx.getText
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
    }
  }

  override
  def visitElasticValueDatatype(ctx: BrioSchemaGrammarParser.ElasticValueDatatypeContext): BrioParseNode = {
    new BrioSchemaElasticDataType {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val blur: Int = ctx.Number(0).getText.toInt
      override val bytes: Int = if (ctx.Number.size > 1) ctx.Number(1).getText.toInt else -1
      override val offsetName: BrioSchemaIdentifier = if (ctx.Identifier != null) ctx.Identifier.extract else null
    }
  }

  override
  def visitLookupValueDatatype(ctx: BrioSchemaGrammarParser.LookupValueDatatypeContext): BrioParseNode = {
    new BrioSchemaLookupDataType {
      override val location: BrioSchemaLocation = BrioSchemaLocation.locate(ctx)
      override val bytes: Int = ctx.Number.getText.toInt
      override val lookupName: BrioSchemaIdentifier = if (ctx.Identifier != null) ctx.Identifier.extract else null
    }
  }

  private
  def extractClassifiers(classifiers: java.util.List[ClassifierContext]) = {
    classifiers.asScala.map {
      c => visit(c).asInstanceOf[BrioSchemaClassifierType]
    }.toArray
  }

}
