/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.function

import org.burstsys.brio.types.BrioTypes.BrioTypeKey
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps
import scala.reflect.{ClassTag, _}

/**
 * An expression consisting of a function call with a domain and range
 */
trait FeltFuncExpr extends FeltExpression {

  /**
   * the name of the function being called
   *
   * @return
   */
  def functionName: String

  /**
   * helpful usage string for troubleshooting parsing problems
   *
   * @return
   */
  def usage: String = "no usage available"

  /**
   * the parameters passed to this function
   *
   * @return
   */
  final var parameters: Array[FeltExpression] = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ parameters.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = parameters

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = parameters.canInferTypes

  override
  def resolveTypes: this.type = {
    parameters.resolveTypes()
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltExpression = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val parameterSource = if (parameters.nonEmpty)
      s"${parameters.map(_.normalizedSource(index)).mkString(", ")}" else ""
    s"$S$functionName($parameterSource)"
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // domain generation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * first generate a set of scopes with each of the parameter expressions generated
   * These will be used for the rest of the setup
   */
  final
  def generatedDomain(implicit cursor: FeltCodeCursor): Array[(FeltCodeCursor, FeltCode)] = parameters.map {
    expr =>
      val pCursor = cursor indentRight 1 scopeDown
      val header = s"${I}var ${pCursor.callScope.scopeNull}:Boolean = false; var ${pCursor.callScope.scopeVal}:${feltType.valueTypeAsCode} = ${feltType.valueDefaultAsCode};"
      val code =
        s"""|
            |$header${expr.generateExpression(pCursor)}""".stripMargin
      (pCursor, code)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // parameter checking
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def fixedParameter[T <: AnyVal : ClassTag](ordinal: Int, name: String): FeltExpression = {
    val expression = parameters(ordinal)
    if (!expression.feltType.isFixedValue)
      throw FeltException(location, s"$name must be fixed number")
    expression.feltType = FeltType.valScal[T]
    expression
  }

  final
  def fixedParameter(bType: BrioTypeKey, ordinal: Int, name: String): FeltExpression = {
    val expression = parameters(ordinal)
    if (!expression.feltType.isFixedValue)
      throw FeltException(location, s"$name must be fixed number")
    expression.feltType = FeltType.valScal(bType)
    expression
  }

  final
  def parameterCountIs(count: Int): Unit = parameterCountIsBetween(count, count)

  final
  def parameterCountAtLeast(lower: Int): Unit = {
    if (parameters.length < lower)
      throw FeltException(location, s"$usage\n\tnot enough parameters ${parameters.length} < ${lower}")
  }

  final
  def parameterCountIsBetween(lower: Int, upper: Int): Unit = {
    if (parameters.length < lower)
      throw FeltException(location, s"$usage\n\tnot enough parameters ${parameters.length} < ${lower}")
    if (parameters.length > upper)
      throw FeltException(location, s"$usage\n\ttoo many parameters ${parameters.length} > ${upper}")
  }

  final
  def parameterAsPath(parameter: Int): FeltPathExpr = {
    parameters.head match {
      case vw: FeltPathExpr => vw
      case _ => throw FeltException(location, s"$usage requires a path parameter")
    }
  }

  final
  def parameterAsReferenceOrThrow[R <: FeltReference : ClassTag](parameter: Int, tag: String): R = {
    val path = parameterAsPath(0)
    path.reference match {
      case None => throw FeltException(location, s"$functionName($path) $tag parameter (0) did not have a reference installed")
      case Some(ref) => ref match {
        case ref: R => ref
        case _ =>
          throw FeltException(location,
            s"$functionName($path) $tag parameter (0) did not contain a ${classTag[R].runtimeClass.getSimpleName} reference")
      }
    }
  }

  final
  def parameterAsReference[R <: FeltReference : ClassTag](parameter: Int, tag: String): Option[R] = {
    val path = parameterAsPath(0)
    path.reference match {
      case None => None
      case Some(ref) => ref match {
        case ref: R => Some(ref)
        case _ =>
          throw FeltException(location,
            s"$functionName($path) $tag parameter (0) did not contain a ${classTag[R].runtimeClass.getSimpleName} reference")
      }
    }
  }

}
