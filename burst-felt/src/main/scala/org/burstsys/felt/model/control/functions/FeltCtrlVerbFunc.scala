/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.functions

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.sweep.symbols.{controlDiscardScopeValue, controlMemberScopeValue, controlRelationScopeValue}
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType


/**
 * base type for all xaction calls
 */
trait FeltCtrlVerbFunc extends FeltFuncExpr {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the brio schema 'node'
   *
   * @return
   */
  private[this]
  var brioNode: BrioNode = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SUBTYPE API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def isRelation: Boolean
  final def isMember: Boolean = !isRelation
  def isAbort: Boolean

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    parameterCountIs(1)
    parameters.head.resolveTypes
    feltType = FeltType.unit
    parameters.head match {
      case path: FeltPathExpr =>
        path.reference foreach {
          case br: FeltBrioStdRef =>
            brioNode = br.refDecl.brioNode
          case _ =>
            throw FeltException(location, s"$functionName(${path.fullPathNoQuotes}) path must be a valid brio reference!")
        }
      case _ =>
        throw FeltException(location, s"$functionName() parameter must be a path expression!")
    }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    resolveTypes
    val pathKey = s"${brioNode.pathKey}; // scopeRoot='${brioNode.pathName}''"
    val frameName = frame.frameName
    s"""|${C(s"assert control verb '$functionName''")}
        |$I${controlRelationScopeValue(frameName)} = ${if (isRelation) pathKey else "-1; // inactive"}
        |$I${controlMemberScopeValue(frameName)} = ${if (isMember) pathKey else "-1; // inactive"}
        |$I${controlDiscardScopeValue(frameName)} = ${if (isAbort) s"true; // active" else s"false // inactive"}; """.stripMargin
  }

}
