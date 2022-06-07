/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate

import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.vitals.strings._

import scala.language.postfixOps

trait FeltSweepTabletGen extends Any {

  def tablets: Array[FeltTabletDecl]

  def global: FeltGlobal

  /**
   *
   * @return
   */
  final
  def genSwpTabletMetadata(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C("tablet globals")}
        |$generateTabletSchemaDeclarations""".stripMargin
  }

  /**
   *
   * @param cursor
   * @return
   */
  final
  def genRtTabletBlk(implicit cursor: FeltCodeCursor): FeltCode = {

    def tablet(tablet: FeltTabletDecl)(implicit cursor: FeltCodeCursor): FeltCode = {
      s"""|
          |${I}var ${tablet.reference.instanceVariable}:$tabletCollectorClassName = _ ; """.stripMargin
    }

    if (tablets.isEmpty) FeltNoCode
    else
      s"""|
          |${C("tablet variables")}
          |${tablets.map(tablet).stringify}""".stripMargin
  }


  private final
  def generateTabletSchemaDeclarations(implicit cursor: FeltCodeCursor): FeltCode = {

    val binding = global.binding.collectors.tablets

    if (tablets.isEmpty) FeltNoCode else {
      val builderClass = binding.builderClassName
      s"""${
        tablets.map {
          tablet =>
            val builderVariable = tablet.reference.builderVariable
            val tabletName = tablet.tabletName
            s"""|
                |${C(s"tablet schema for '$tabletName'")}
                |${I}val $builderVariable:$builderClass = ${tablet.generateDeclaration}""".stripMargin
        }.stringify
      }""".stripMargin
    }
  }


}
