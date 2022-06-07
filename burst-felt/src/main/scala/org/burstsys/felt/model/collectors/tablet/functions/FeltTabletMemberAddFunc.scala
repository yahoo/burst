/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.functions

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletRef
import org.burstsys.felt.model.expressions._
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types._

import scala.language.postfixOps

object FeltTabletMemberAddFunc {
  final val functionName: String = "tabletMemberAdd"
}

/**
 * add a value to a tablet
 */
trait FeltTabletMemberAddFunc extends FeltTabletFunction with FeltBoolExpr {

  final override val nodeName = "felt-tablet-member-add-func"

  final val functionName: String = FeltTabletMemberAddFunc.functionName

  final override val usage: String =
    s"""
       |usage:  $functionName(tablet_name, value_expression) -> unit
       |    add a value to a tablet""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    super.resolveTypes
    feltType = FeltType.unit
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // code generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(2)

    // get route parameter
    val tablet = parameterAsReferenceOrThrow[FeltTabletRef](0, "tablet reference")
    val valueType = tablet.refDecl.feltType.valueType

    // get vale parameter
    val valueExpr = fixedParameter(valueType, 1, "tablet value")
    val valueExprCursor = cursor indentRight 1 scopeDown
    val valueExpressionValue = valueExprCursor.callScope.scopeVal


    val instanceVariable = tablet.instanceVariable

    val op = valueType match {
      case BrioBooleanKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddBoolean($valueExpressionValue )"
      case BrioByteKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddByte( $valueExpressionValue )"
      case BrioShortKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddShort( $valueExpressionValue )"
      case BrioIntegerKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddInteger( $valueExpressionValue )"
      case BrioLongKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddLong( $valueExpressionValue )"
      case BrioDoubleKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddDouble( $valueExpressionValue )"
      case BrioStringKey => s"$sweepRuntimeSym.$instanceVariable.tabletAddString( $valueExpressionValue )( $sweepRuntimeSym, runtime.dictionary )"
    }

    s"""|
        |${T(this)}
        |${expressionEvaluate(valueExpr, s"$nodeName-value-expr")(valueExprCursor)}
        |${I}if( !${valueExprCursor.callScope.scopeNull}  ) {
        |$I2$op ;
        |$I}""".stripMargin
  }

}
