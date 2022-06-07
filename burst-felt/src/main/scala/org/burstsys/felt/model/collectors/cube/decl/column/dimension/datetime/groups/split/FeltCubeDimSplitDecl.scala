/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimColSem, FeltCubeDimDecl, FeltDimSemType}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

/**
 * A Felt compatible ''split'' dimension AST node
 */
trait FeltCubeDimSplitDecl extends FeltCubeDimDecl {

  final override val nodeName = "felt-cube-split-dim"

  /**
   * TODO
   *
   * @return
   */
  def splitValues: Array[FeltExpression]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this) ++ splitValues.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = splitValues

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override val semanticType: FeltDimSemType = SPLIT_DIMENSION_SEMANTIC

  override lazy val semantic: FeltCubeDimColSem = new FeltCubeDimSplitSem {
    override val bType: BrioTypeKey = valueType
    override val columnName: BrioRelationName = FeltCubeDimSplitDecl.this.columnName

    override def semanticRt: FeltCubeDimSplitSemRt =
      valueType match {
        case BrioBooleanKey =>
          FeltCubeDimBooleanSplitSemRt(extractValues[Boolean](splitValues))
        case BrioByteKey =>
          FeltCubeDimByteSplitSemRt(extractValues[Byte](splitValues))
        case BrioShortKey =>
          FeltCubeDimShortSplitSemRt(extractValues[Short](splitValues))
        case BrioIntegerKey =>
          FeltCubeDimIntSplitSemRt(extractValues[Int](splitValues))
        case BrioLongKey =>
          FeltCubeDimLongSplitSemRt(extractValues[Long](splitValues))
        case BrioDoubleKey =>
          FeltCubeDimDoubleSplitSemRt(extractValues[Double](splitValues))
        case BrioStringKey =>
          FeltCubeDimStringSplitSemRt(extractValues[String](splitValues))
        case t => throw FeltException(location, s" unknown typekey=$valueType")
      }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimSplitDecl = new FeltCubeDimSplitDecl {
    sync(FeltCubeDimSplitDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimSplitDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimSplitDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimSplitDecl.this.location
    final override val splitValues: Array[FeltExpression] = FeltCubeDimSplitDecl.this.splitValues.map(_.resolveTypes.reduceStatics)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${
      semanticType.name
    }[${brioDataTypeNameFromKey(valueType).toLowerCase}](${
      splitValues.map(_.normalizedSource).mkString(", ")
    })"

}
