/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimDecl, FeltDimSemType}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.source.S

/**
 * A Felt compatible ''verbatim'' dimension AST node
 */
trait FeltCubeDimVerbatimDecl extends FeltCubeDimDecl {

  final override val nodeName = "felt-cube-verbatim-dim"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override val semanticType: FeltDimSemType = VERBATIM_DIMENSION_SEMANTIC

  final override lazy val semantic: FeltCubeDimVerbatimSem =
    new FeltCubeDimVerbatimSem {
      final override val bType: BrioTypeKey = FeltCubeDimVerbatimDecl.this.valueType
      final override val columnName: BrioRelationName = FeltCubeDimVerbatimDecl.this.columnName
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimVerbatimDecl = new FeltCubeDimVerbatimDecl {
    sync(FeltCubeDimVerbatimDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimVerbatimDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimVerbatimDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimVerbatimDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
