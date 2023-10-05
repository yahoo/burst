/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.parser

import org.burstsys.brio.grammar.BrioSchemaGrammarParser.SchemaClauseContext
import org.burstsys.brio.grammar.{BrioSchemaGrammarLexer, BrioSchemaGrammarParser}
import org.burstsys.brio.model.BrioParserErrorListener
import org.burstsys.brio.types.BrioTypes.{BrioSchemaName, BrioVersionKey}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode
import org.apache.logging.log4j.Logger

object BrioSchemaParser {

  /**
    *
    */
  type BrioSchemaOrdinal = Int

  /**
    *
    */
  type BrioSchemaIdentifier = String

  /**
    *
    */
  type BrioSchemaPrimitive = String

  implicit final class ParsedIdentifier(val identifier: TerminalNode) extends AnyVal {
    def extract: BrioSchemaIdentifier = identifier.getText.stripPrefix("\"").stripPrefix("'").stripSuffix("\"").stripSuffix("'")
  }

  trait BrioParseNode extends Any {
    def location: BrioSchemaLocation
  }

  trait BrioSchemaRootClause extends BrioParseNode {

    def rootFieldName: BrioSchemaIdentifier

    def rootTypeName: BrioSchemaIdentifier

    def rootType: BrioSchemaStructureClause
  }

  trait BrioSchemaClause extends BrioParseNode {
    def name: BrioSchemaName

    def version: BrioVersionKey

    def root: BrioSchemaRootClause

    def structures: Array[BrioSchemaStructureClause]
  }

  trait BrioSchemaStructureClause extends BrioParseNode {
    def version: BrioVersionKey

    def version_=(v: BrioVersionKey): Unit

    def selfName: BrioSchemaIdentifier

    def superTypeName: BrioSchemaIdentifier

    def superType: BrioSchemaStructureClause

    def superType_=(s: BrioSchemaStructureClause): Unit

    def relations: Array[BrioSchemaRelationClause]
  }

  trait BrioSchemaRelationClause extends BrioParseNode {
    def relationOrdinal: BrioSchemaOrdinal

    def relationName: BrioSchemaIdentifier

    def relationClassifiers: Array[BrioSchemaClassifierType]
  }

  trait BrioSchemaClassifierType extends BrioParseNode {
    def name: String

    def isKey: Boolean = name == "key"

    def isOrdinal: Boolean = name == "ordinal"
  }

  trait BrioSchemaKeyClassifier extends BrioSchemaClassifierType {
    val name: String = "key"
  }

  trait BrioSchemaOrdinalClassifier extends BrioSchemaClassifierType {
    val name: String = "ordinal"
  }

  trait BrioSchemaValueRelation extends BrioSchemaRelationClause {
    def valueDatatype: BrioSchemaDataTypeClause
  }

  trait BrioSchemaValueScalarRelation extends BrioSchemaValueRelation

  trait BrioSchemaValueMapRelation extends BrioSchemaValueRelation {
    def keyDatatype: BrioSchemaDataTypeClause
  }

  trait BrioSchemaValueVectorRelation extends BrioSchemaValueRelation

  trait BrioSchemaReferenceRelation extends BrioSchemaRelationClause {
    def referenceTypeName: BrioSchemaIdentifier

    def referenceType: BrioSchemaStructureClause

    def referenceType_=(r: BrioSchemaStructureClause): Unit
  }

  trait BrioSchemaReferenceScalarRelation extends BrioSchemaReferenceRelation

  trait BrioSchemaReferenceVectorRelation extends BrioSchemaReferenceRelation

  trait BrioSchemaDataTypeClause extends BrioParseNode

  trait BrioSchemaSimpleDataType extends BrioSchemaDataTypeClause {
    def primitive: BrioSchemaPrimitive
  }

  trait BrioSchemaExtendedDataType extends BrioSchemaDataTypeClause {
    def bytes: Int
  }

  trait BrioSchemaElasticDataType extends BrioSchemaExtendedDataType {
    def blur: Int

    def offsetName: String
  }

  trait BrioSchemaLookupDataType extends BrioSchemaExtendedDataType {
    def lookupName: String
  }

}

final case
class BrioSchemaParser(log:Logger) {
  import BrioSchemaParser._

  // this has to be java for some odd reason...
  private val errorListener: ANTLRErrorListener = new BrioParserErrorListener(log)

  /**
    * parse a brio schema
    *
    * @param schemaSpecification
    * @return
    */
  def parse(schemaSpecification: String): BrioSchemaClause = try {
    val lexer = new BrioSchemaGrammarLexer(CharStreams.fromString(schemaSpecification))
    val tokenStream = new CommonTokenStream(lexer)
    val parser = new BrioSchemaGrammarParser(tokenStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(errorListener)
    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)
    var tree: SchemaClauseContext = null
    try { // first, try parsing with potentially faster SLL mode
      parser.getInterpreter.setPredictionMode(PredictionMode.SLL)
      tree = parser.schemaClause()
    } catch safely {
      case ex: ParseCancellationException =>
        // if we fail, parseExpression with LL mode
        tokenStream.seek(0) // rewind input stream
        parser.reset()
        parser.getInterpreter.setPredictionMode(PredictionMode.LL)
        tree = parser.schemaClause()
    }
    bind(BrioSchemaBuilder(tree).build)
  } catch safely {
    case e: StackOverflowError =>
      throw BrioSchemaException("too large (stack overflow while parsing)")
    case t: Throwable =>
      throw VitalsException(t)
  }

  private
  def bind(schema: BrioSchemaClause): BrioSchemaClause = {
    // convenient lookup for types
    val structureMap = schema.structures.map(s => s.selfName -> s).toMap
    // bind super types
    schema.structures foreach {
      s =>
        if (s.superTypeName != null) s.superType = structureMap.getOrElse(
          s.superTypeName, throw BrioSchemaException(s, s"unknown supertype ${s.superTypeName}")
        )
        s.version = schema.version
    }
    // find relation types
    schema.structures.flatMap(_.relations) foreach {
      case s: BrioSchemaValueMapRelation =>
      case s: BrioSchemaValueRelation =>
      case s: BrioSchemaReferenceRelation =>
        if (s.referenceTypeName != null) s.referenceType = structureMap.getOrElse(
          s.referenceTypeName,
          throw BrioSchemaException(s, s"unknown type '${s.referenceTypeName}'")
        )
    }
    schema
  }

}
