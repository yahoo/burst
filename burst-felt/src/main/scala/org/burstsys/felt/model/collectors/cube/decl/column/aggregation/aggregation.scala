/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column

import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeColSem, FeltSemType}

package object aggregation {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // aggregate functions
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private[felt] final val MinName = "min"
  private[felt] final val MaxName = "max"
  private[felt] final val SumName = "sum"
  private[felt] final val UniqueName = "unique"
  private[felt] final val TopName = "top"
  private[felt] final val BottomName = "bottom"
  private[felt] final val ProjectName = "project"

  /**
   * abstraction for all aggregations in DSL
   *
   */
  abstract
  class FeltCubeAggColSem extends FeltCubeColSem {

    def semanticRt: FeltCubeAggSemRt

  }

  /**
   * ENUM type for identifying aggregation semantics
   *
   */
  abstract
  class FeltAggSemType(nm: String) extends FeltSemType(nm) {
    final val id: String = name;
  }

  object SUM_AGGREGATION_SEMANTIC extends FeltAggSemType(SumName)

  object MIN_AGGREGATION_SEMANTIC extends FeltAggSemType(MinName)

  object MAX_AGGREGATION_SEMANTIC extends FeltAggSemType(MaxName)

  object UNIQUE_AGGREGATION_SEMANTIC extends FeltAggSemType(UniqueName)

  object TOP_AGGREGATION_SEMANTIC extends FeltAggSemType(TopName)

  object BOTTOM_AGGREGATION_SEMANTIC extends FeltAggSemType(BottomName)

  object PROJECT_AGGREGATION_SEMANTIC extends FeltAggSemType(ProjectName)


}
