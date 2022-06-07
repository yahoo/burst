/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.brio.model.schema.types.BrioValueMapRelation
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.types.FeltType


object FeltBrioKeysFunc {
  final val functionName: String = "keys"
}

/**
 * return a value vector containing key datatype values found in the type provided in the
 * first parameter
 */
trait FeltBrioKeysFunc extends FeltBrioFunction {
  final override val nodeName = "felt-brio-keys-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_value_map>):value_vector
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    parameters.foreach(_.resolveTypes)
    feltType = FeltType.valVec(parameters.head.feltType.keyType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val brioRef = parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val decl = brioRef.refDecl
    if (decl.relation.relationForm != BrioValueMapRelation)
      throw FeltException(location, s"$usage path parameter must refer to a value map")
    s"""|
        |$I{ // ${M(this)}
        |$I{ // FELT-BRIO keys(${decl.brioNode.pathName}) call, scope=${cursor.callScope}
        |$I2//???
        |$I}""".stripMargin
  }
}
