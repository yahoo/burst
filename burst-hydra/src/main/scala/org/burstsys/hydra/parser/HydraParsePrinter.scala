/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.analysis.generate.FeltAnalysisSweepGenerator
import org.burstsys.felt.model.tree.code.FeltCodeCursor
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.{FeltGlobal, FeltNode}

/**
  * print routines for parser
  */
trait HydraParsePrinter extends Any {

  self: HydraParser =>

  final
  def printParse[N <: FeltNode](f: HydraParser => N)(implicit source: String, defaultSchema: BrioSchema): N = {
    log info
      s"""
         |
         |----------- SOURCE ------------------
         |${source.trim}
         |------------------------------------------------""".stripMargin
    val clause1 = f(this)
    val normalizedSource1 = clause1.normalizedSource()
    log info
      s"""
         |
         |----------- NORMALIZED SOURCE ------------------
         |$normalizedSource1
         |------------------------------------------------""".stripMargin
    clause1 match {
      case fe: FeltExpression =>
        val os = fe.reduceStatics.normalizedSource
        log info
          s"""
             |
             |----------- OPTIMIZED EXPRESSION ------------------
             |$os
             |------------------------------------------------""".stripMargin
      case fd: FeltDeclaration =>
        val os = fd.reduceStatics.normalizedSource
        log info
          s"""
             |
             |----------- OPTIMIZED DECLARATION ------------------
             |$os
             |------------------------------------------------""".stripMargin
      case _ =>
    }

    clause1
  }

  final
  def printGeneration[N <: FeltNode](f: HydraParser => N)(implicit source: String, defaultSchema: BrioSchema): N = {
    log info
      s"""
         |
         |----------- SOURCE ------------------
         |${source.trim}
         |------------------------------------------------""".stripMargin
    val clause1 = f(this)
    val code1 = clause1 match {
      case a: FeltAnalysisDecl =>  ""
      case e: FeltExpression => e.generateExpression(FeltCodeCursor(defaultSchema))
      case d: FeltDeclaration => d.generateDeclaration(FeltCodeCursor(defaultSchema))
      case _ => ???
    }
    val normalizedSource1 = clause1.normalizedSource()
    log info
      s"""
         |
         |----------- NORMALIZED SOURCE ------------------
         |$normalizedSource1
         |------------------------------------------------""".stripMargin
    val clause2 = f(this)
    clause1 match {
      case fe: FeltExpression =>
        val reduction = fe.reduceStatics
        val os = reduction.normalizedSource
        log info
          s"""
             |
             |----------- OPTIMIZED SOURCE ------------------
             |$os
             |------------------------------------------------""".stripMargin
      case fd: FeltDeclaration =>
        val os = fd.reduceStatics.normalizedSource
        log info
          s"""
             |
             |----------- OPTIMIZED DECLARATION ------------------
             |$os
             |------------------------------------------------""".stripMargin
      case _ =>
    }
    val normalizedSource2 = clause2.normalizedSource()
    val code2 = clause1 match {
      case analysis: FeltAnalysisDecl  =>
        FeltAnalysisSweepGenerator(analysis = analysis).generateSweep(FeltCodeCursor(analysis.global))
      case e: FeltExpression => e.generateExpression(FeltCodeCursor(defaultSchema))
      case d: FeltDeclaration => d.generateDeclaration(FeltCodeCursor(defaultSchema))
      case _ => ???
    }
    log info
      s"""
         |
         |----------- GENERATED CODE ------------------
         |$code2
         |------------------------------------------------""".stripMargin
    clause2
  }


}
