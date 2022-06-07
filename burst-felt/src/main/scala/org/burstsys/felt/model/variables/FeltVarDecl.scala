/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.variables

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.types.FeltTypeDecl

import scala.reflect.ClassTag

/**
 * A type declaration for a variable (these have appropriate forms of references installed)
 */
trait FeltVarDecl extends FeltRefDecl {

  /**
   * This is a static determinable semantic
   *
   * @return
   */
  def isMutable: Boolean

  /**
   * This is a static determinable semantic
   *
   * @return
   */
  def typeDeclaration: FeltTypeDecl

  /**
   * this may be static (if reducible to a constant construct) or may be
   * dynamic and has to be evaluated at runtime.
   *
   * @return
   */
  def initializer: FeltExpression

  final lazy val varName = s"""'${refName.fullPathNoQuotes}'"""

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ initializer.treeApply(rule) ++ refName.treeApply(rule) ++ typeDeclaration.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = initializer.asArray ++ refName.asArray ++ typeDeclaration.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = typeDeclaration.canInferTypes && initializer.canInferTypes

  final override
  def resolveTypes: this.type = {
    typeDeclaration.resolveTypes
    refName.resolveTypes
    initializer.resolveTypes

    if (refName.reference.nonEmpty) {
      // make sure both sides match
      if (typeDeclaration.feltType.form != initializer.feltType.form)
        throw FeltException(location, s"$printSource inappropriate initializer (${typeDeclaration.feltType.form} != ${initializer.feltType.form})")

      if (refName.isMutable && (typeDeclaration.feltType.form.isMap || typeDeclaration.feltType.form.isVector))
        throw FeltException(location, s"$printSource maps and vectors can't be mutable (must be val)")

    }

    feltType = typeDeclaration.feltType

    typeDeclaration.inferInitializer(initializer)

    refName.feltType = feltType
    /* TODO type compatibility
        if (initializer.feltType.valueNotNull && initializer.feltType != feltType)
          throw FeltException(location,
            s"FELT-VAR-DECL initializer/variable type mismatch ${initializer.feltType} -> $feltType"
          )
    */
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def normalizedSource(implicit index: Int): String =
    s"$S${if (isMutable) "var" else "val"} ${refName.fullPathAndKey}:${typeDeclaration.normalizedSource} = ${initializer.normalizedSource}"

}
