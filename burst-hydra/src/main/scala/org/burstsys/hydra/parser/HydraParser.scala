/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.hydra.grammar.{HydraAnalysisGrammarLexer, HydraAnalysisGrammarParser}
import org.burstsys.hydra.parser.builder.HydraParseAnalysisBldr
import org.burstsys.hydra.sweep.HydraFeltBinding
import org.burstsys.vitals.errors.{VitalsException, _}
import org.antlr.v4.runtime._

import scala.language.postfixOps

/**
 * The wrapper for the antlr hydra source language parser
 */
final case
class HydraParser() extends AnyRef with HydraParsePoints with HydraParsePrinter {

  protected
  def doParse(source: String, defaultSchema: BrioSchema, treeGenerator: HydraAnalysisGrammarParser => ParserRuleContext): FeltAnalysisDecl = {
    val errorListener = HydraParserErrorHandler(source)
    try {
      val global = FeltGlobal(brioSchema = defaultSchema, source = source, binding = HydraFeltBinding)
      val lexer = new HydraAnalysisGrammarLexer(CharStreams.fromString(source))
      lexer removeErrorListeners()
      lexer addErrorListener errorListener
      val tokenStream = new CommonTokenStream(lexer)
      val parser = new HydraAnalysisGrammarParser(tokenStream)
      parser.removeErrorListeners()
      parser addErrorListener errorListener
      try {
        val context = treeGenerator(parser)
        errorListener.throwIfError(global)

        val parsedTree = HydraParseAnalysisBldr(global) visit context
        errorListener.throwIfError(global)

        parsedTree match {
          case a: FeltAnalysisDecl => a.activate()
          case n => throw FeltException(n.location, s"parser did not return an analysis - instead it returned a '${n.nodeName}''")
        }
      } catch safely {
        case bpe: FeltException =>
          throw bpe
        case t: Throwable =>
          errorListener.throwIfError(global)
          throw t
      }
    } catch safely {
      case bpe: FeltException =>
        throw bpe
      case _: StackOverflowError =>
        throw VitalsException("HYDRA_PARSE_OVERFLOW (stack overflow while parsing)")
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
