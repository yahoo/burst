/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.generate

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.felt.model.schema.traveler.FeltTravelerGenerator
import org.burstsys.felt.model.sweep.symbols.brioPathKeyClass
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I, I2, I3, I4}
import org.burstsys.vitals.strings.VitalsGeneratingArray

/**
 * schema related code generation for 'control verbs'
 */
trait FeltCtrlTravGen {

  /**
   * generate control verb schema artifacts
   *
   * @param cursor
   * @return
   */
  def generateControlSchema(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltCtrlTravGen {
  def apply(generator:FeltTravelerGenerator): FeltCtrlTravGen =
    FeltCtrlTravGenContext(generator)
}

private final case
class FeltCtrlTravGenContext(generator:FeltTravelerGenerator) extends FeltCtrlTravGen {

  override
  def generateControlSchema(implicit cursor: FeltCodeCursor): FeltCode = {

    val allVisitNodes = cursor.schema.brioSchema.allNodesForForms(
      BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation
    )

    def scopeKeyCaseCode(visitNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
      val ancestors = cursor.schema.brioSchema.nodeForPathKey(visitNode.pathKey).ancestors.map(_.pathKey)
      allVisitNodes.map { node =>
        if (visitNode == node || ancestors.contains(node.pathKey)) s"${node.pathKey} // SCOPE: ${node.pathName}"
        else null
      }.noNulls.mkString(s"\n${I}OR ")
    }

    def visitKeyMatches(implicit cursor: FeltCodeCursor): FeltCode = {
      allVisitNodes.map { node =>
        val caseCode = scopeKeyCaseCode(node)(cursor indentRight 3)

        val scopeKeyCases = if (caseCode.nonEmpty)
          s"""|
              |${I3}case ${caseCode}
              |${I4}=> true""".stripMargin
        else FeltNoCode

        s"""|
            |${I}case ${node.pathKey} => // VISIT: ${node.pathName}
            |${I2}scopeKey match {$scopeKeyCases
            |${I3}case _ => false
            |${I2}}""".stripMargin
      }.mkString
    }

    s"""|
        |${C("support test for path in scope (on axis and below)")}
        |${I}@inline override
        |${I}def visitInScope(visitKey: $brioPathKeyClass, scopeKey: $brioPathKeyClass): Boolean = {
        |${I2}if(visitKey == -1 || scopeKey == -1) return false;
        |${I2}visitKey match {${visitKeyMatches(cursor indentRight 2)}
        |${I3}case _ => ???
        |${I2}}
        |${I}}""".stripMargin

  }
}
