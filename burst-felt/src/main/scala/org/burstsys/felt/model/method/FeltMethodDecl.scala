/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.method

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.expressions.FeltExprBlock
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltTypeDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl
import org.burstsys.vitals.strings._

import scala.reflect.ClassTag


/**
 * IN PROGRESS FEATURE
 *
 *
 */
trait FeltMethodDecl extends FeltDeclaration {

  final override val nodeName = "felt-method-decl"

  /**
   * the name of this method
   *
   * @return
   */
  def methodName: String

  /**
   * the list of optional parameters for this method
   *
   * @return
   */
  def parameters: Array[FeltParamDecl]

  /**
   * the associated expression block (the method contents)
   *
   * @return
   */
  def expressionBlock: FeltExprBlock

  /**
   * the return type for this method
   *
   * @return
   */
  def returnType: FeltTypeDecl

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ parameters.treeApply(rule) ++ expressionBlock.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = parameters ++ expressionBlock.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = parameters.canInferTypes && expressionBlock.canInferTypes && returnType.canInferTypes

  final override
  def resolveTypes: this.type = {
    parameters.foreach(_.resolveTypes)
    expressionBlock.resolveTypes
    returnType.resolveTypes
    feltType = returnType.feltType
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltMethodDecl = new FeltMethodDecl {
    sync(FeltMethodDecl.this)
    final override val methodName: String = FeltMethodDecl.this.methodName
    final override val parameters: Array[FeltParamDecl] = FeltMethodDecl.this.parameters.map(_.reduceStatics.resolveTypes)
    final override val expressionBlock: FeltExprBlock = FeltMethodDecl.this.expressionBlock.reduceStatics.resolveTypes
    final override val returnType: FeltTypeDecl = FeltMethodDecl.this.returnType
    final override val location: FeltLocation = FeltMethodDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"""${S}def $methodName(${
      parameters.map(_.normalizedSource).mkString(", ")
    }):${returnType.normalizedSource} = ${
      expressionBlock.normalizedSource(index).trimAtBegin
    }""".stripMargin.trimAtEnd

}
