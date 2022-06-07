/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema.decl

import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.BrioRelationName
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

trait FeltSchemaExtension extends FeltNode {

  final override val nodeName: String = "felt-schema-extension-decl"

  private[this]
  var _extendedKey: BrioPathKey = _

  /**
   * the static schema relation part
   *
   * @return
   */
  def schemaPath: FeltPathExpr

  /**
   * the 'extension' to the static schema relation
   *
   * @return
   */
  def schemaExtension: FeltPathExpr

  final def extendedSuffix: BrioPathName = schemaExtension.fullPath

  final def relationName: BrioRelationName = schemaExtension.fullPath

  final def extendedPath: BrioPathName = s"${schemaPath.fullPath}.${schemaExtension.fullPath}"

  final def extendedKey: BrioPathKey = _extendedKey

  final def extendedKey_=(key: BrioPathKey): Unit = _extendedKey = key

  final def parentKey: BrioPathKey = global.brioSchema.nodeForPathName(schemaPath.fullPath).pathKey // must be in brio schema

  final def parentPath: BrioPathName = global.brioSchema.nodeForPathName(schemaPath.fullPath).pathName

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltSchemaExtension = new FeltSchemaExtension {
    sync(FeltSchemaExtension.this)
    final override val location: FeltLocation = FeltSchemaExtension.this.location
    final override val schemaPath: FeltPathExpr = FeltSchemaExtension.this.schemaPath.reduceStatics.resolveTypes
    final override val schemaExtension: FeltPathExpr = FeltSchemaExtension.this.schemaExtension.reduceStatics.resolveTypes

    extendedKey = FeltSchemaExtension.this.extendedKey
  }

  final override
  def resolveTypes: this.type = {
    schemaPath.resolveTypes
    schemaExtension.resolveTypes
    this
  }

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ schemaPath.treeApply(rule) ++ schemaExtension.treeApply(rule)

  final override
  def canInferTypes: Boolean = schemaPath.canInferTypes && schemaExtension.canInferTypes

  final override
  def normalizedSource(implicit index: Int): String = {
    s"""${schemaPath.fullPath} <- ${schemaExtension.fullPath}"""
  }

}
