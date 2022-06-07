/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.brio.model.schema.types._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols.{latticeValueMapValueViaSweepRuntime, latticeValueVectorValueIsNullViaSweepRuntime, latticeValueVectorValueViaSweepRuntime}
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, I2, M}
import org.burstsys.felt.model.types.FeltType

object FeltBrioValueFunc {
  final val functionName: String = "value"
}

trait FeltBrioValueFunc extends FeltBrioFunction {
  final override val nodeName = "felt-brio-value-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_value_map>) -> boolean
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
    val decl = brioRef.refDecl
    val pathName = decl.brioNode.pathName
    decl.relation.relationForm match {

      case BrioValueVectorRelation =>
        s"""|
            |$I{ // ${M(this)}
            |$I2${cursor.callScope.scopeVal} = ${latticeValueVectorValueViaSweepRuntime(pathName)}
            |$I2${cursor.callScope.scopeNull} = ${latticeValueVectorValueIsNullViaSweepRuntime(pathName)}
            |$I}""".stripMargin


      case BrioValueMapRelation =>
        s"""|
            |$I{ // ${M(this)}
            |$I2${cursor.callScope.scopeVal} = ${latticeValueMapValueViaSweepRuntime(pathName)}
            |$I}""".stripMargin

      case BrioValueScalarRelation => throw FeltException(location, s"$functionName() BrioValueScalarRelation not supported")
      case BrioReferenceScalarRelation => throw FeltException(location, s"$functionName() BrioReferenceScalarRelation not supported")
      case BrioReferenceVectorRelation => throw FeltException(location, s"$functionName() BrioReferenceVectorRelation not supported")
      case _ => ???
    }
  }

}
