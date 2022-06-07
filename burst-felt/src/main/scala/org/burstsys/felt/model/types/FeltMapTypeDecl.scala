/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.types

import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioTypeKey, brioDataTypeNameFromKey, scalaTypeFromBrioTypeKey}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.mutable.FeltValMapLit
import org.burstsys.felt.model.mutables.valmap.FeltMutableValMap
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.vitals.strings.VitalsString

/**
 * A type declaration for a ''map'' of primitive value types
 *
 * @see [[FeltMutableValMap]]
 */
trait FeltMapTypeDecl extends FeltTypeDecl {

  final override val nodeName = "felt-map-type-decl"

  /**
   * the associated brio value type for the ''value'' part of the key/value association
   *
   * @return
   */
  def valueType: BrioTypeKey

  /**
   * the associated brio value type for the ''key'' part of the key/value association
   *
   * @return
   */
  def keyType: BrioTypeKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = true

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valMap(valueType, keyType)
    this
  }

  final override
  def inferInitializer(initializer: FeltExpression): FeltType = {
    initializer match {
      case lit: FeltValMapLit =>
        val keyType = initializer.feltType.keyType
        val valueType = initializer.feltType.valueType
        if (keyType == BrioTypes.BrioUnitTypeKey && valueType == BrioTypes.BrioUnitTypeKey) {
          // an untyped (empty) map
          initializer.feltType = this.feltType
        } else {
          // TODO try to rationalize decl and initializer
        }
        this.feltType
      case _ =>
        throw FeltException(location, s"'${normalizedSource.condensed}' must use map literal to initialize map var")
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateType(implicit cursor: FeltCodeCursor): FeltCode =
    s"""scala.Map[${scalaTypeFromBrioTypeKey(keyType)}, ${scalaTypeFromBrioTypeKey(valueType)}]""".stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"map[${brioDataTypeNameFromKey(keyType).toLowerCase}, ${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
