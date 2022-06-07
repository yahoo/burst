/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column

import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeColSem, FeltCubeDecl}
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.strings.{VitalsGeneratingArray, VitalsString}

import scala.reflect.ClassTag

/**
 * A set of cube columns
 */
trait FeltCubeColsNode[C <: FeltCubeColDecl[_ <: FeltCubeColSem]] extends FeltNode {

  /**
   *
   * @return
   */
  def columns: Array[C]

  /**
   *
   * @return
   */
  def keyName: String

  final
  def cubeDecl: FeltCubeDecl = _cubeDecl

  final
  def cubeDecl_=(c: FeltCubeDecl): Unit = {
    _cubeDecl = c
  }

  ///////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////

  private[this]
  var _cubeDecl: FeltCubeDecl = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    if (!canInferTypes) return this
    columns.resolveTypes()
    feltType = FeltType.unit
    this
  }

  final override
  def canInferTypes: Boolean = true

  ///////////////////////////////////////////////////////////////////////
  // TREE OPS
  ///////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ columns.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = columns

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    if (columns.nonEmpty)
      s"\n${S}$keyName {\n${columns.map(_.normalizedSource(index + 1).singleLineEnd).stringify.trimAtEnd}\n$S}"
    else ""
  }

}
