/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggDecl
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.tree.source._

import scala.reflect.ClassTag

/**
 * A Felt compatible ''top'' or ''bottom'' take aggregate AST node
 */
trait FeltCubeAggTakeDecl extends FeltCubeAggDecl {

  def mode: FeltCubeTakeSemMode

  /**
   * TODO
   *
   * @return
   */
  def topValues: Array[FeltExpression]

  override final lazy val semantic: FeltCubeAggTopColSem = {
    topValues.foreach(_.reduceToFixAtomOrThrow)
    //    topValues.activate()
    val parameters = topValues.map(_.reduceToFixAtom.get.value.toInt)
    topValues.length match {
      case 1 => // scatter
        new FeltCubeAggTopColSem {
          sync(FeltCubeAggTakeDecl.this)
          final override val bType: BrioTypeKey = feltType.valueType
          final override val columnName: BrioRelationName = FeltCubeAggTakeDecl.this.columnName
          final override val scatterK: BrioVersionKey = parameters(0)
        }
      case 2 => // scatter, slice
        new FeltCubeAggTopColSem {
          sync(FeltCubeAggTakeDecl.this)
          final override val bType: BrioTypeKey = feltType.valueType
          final override val columnName: BrioRelationName = FeltCubeAggTakeDecl.this.columnName
          final override val scatterK: BrioVersionKey = parameters(0)
          final override val sliceK: BrioVersionKey = parameters(1)
        }
      case 3 => // scatter, slice, item
        new FeltCubeAggTopColSem {
          sync(FeltCubeAggTakeDecl.this)
          final override val bType: BrioTypeKey = feltType.valueType
          final override val columnName: BrioRelationName = FeltCubeAggTakeDecl.this.columnName
          final override val scatterK: BrioVersionKey = parameters(0)
          final override val sliceK: BrioVersionKey = parameters(1)
          final override val itemK: BrioVersionKey = parameters(2)
        }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this) ++ topValues.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = topValues

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = super.canInferTypes && topValues.canInferTypes

  override
  def resolveTypes: this.type = {
    super.resolveTypes
    topValues.foreach(_.resolveTypes)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}](${
      topValues.map(_.normalizedSource).mkString(",")
    })"

}
