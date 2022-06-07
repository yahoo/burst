/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggPrimitiveDecl
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltAggSemType, FeltCubeAggsNode}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim.FeltCubeDimVerbatimDecl
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimsNode, FeltDimSemType}
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.vitals.logging._

package object magic extends VitalsLogger {

  final case
  class Dim(name: String, override val columnOrdinal: Int) extends FeltCubeDimVerbatimDecl {
    override def valueType: BrioTypeKey = BrioLongKey
    override def refName: FeltPathExpr = FeltSimplePath(name)
  }

  final case
  class Agg(name: String, override val columnOrdinal: Int) extends FeltCubeAggPrimitiveDecl {
    override def valueType: BrioTypeKey = BrioLongKey
    override def refName: FeltPathExpr = FeltSimplePath(name)

    override def semanticType: FeltAggSemType = ???

    override def semantic: aggregation.FeltCubeAggColSem = ???

    override def nodeName: String = ???
  }

  final case
  class Cube( name:String, dims: Array[Dim], aggs: Array[Agg], override val children: Array[Cube] = Array.empty) extends FeltCubeDecl {
    override def aggregations: FeltCubeAggsNode = ???

    override def dimensions: FeltCubeDimsNode = ???

    override def subCubes: Array[FeltCubeDecl] = ???

    override def refTarget: FeltPathExpr = ???
  }


}
