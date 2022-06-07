/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.brio.model.schema.types.BrioValueMapRelation
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.types.FeltType

object FeltBrioValuesFunc {
  final val functionName: String = "values"
}

/**
 * return a value vector containing the values in a relationship or literal provided in the first parameter
 */
trait FeltBrioValuesFunc extends FeltBrioFunction {
  final override val nodeName = "felt-brio-values-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_value_map>) -> value_vector
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    parameters.foreach(_.resolveTypes)
    feltType = FeltType.valScal(parameters.head.feltType.valueType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val brioRef = parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val node = brioRef.refDecl
    if (node.relation.relationForm != BrioValueMapRelation)
      throw FeltException(location, s"$usage path parameter must refer to a value map")
    s"""|
        |$I{ // ${M(this)}
        |$I2???
        |$I}""".stripMargin
  }

}
