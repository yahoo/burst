/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

package object primitive {

  abstract
  class FeltCubeAggPrimColSem extends FeltCubeAggColSem

  /**
   * Your classic sum aggregation
   *
   */
  abstract
  class FeltCubeAggSumColSem extends FeltCubeAggPrimColSem {
    final override val semanticRt: FeltCubeAggSumSemRt = FeltCubeAggSumSemRt()
  }

  /**
   * a special aggregation that is a simple update with no further operations.
   *
   */
  abstract
  class FeltCubeAggProjectColSem extends FeltCubeAggPrimColSem {
    final override val semanticRt: FeltCubeAggProjectSemRt = FeltCubeAggProjectSemRt()
  }


  /**
   * The Unique semantic has two separate semantics depending on whether the aggregation is happening within an item
   * traversal, or across items. In the former case, the semantic is a ceiling of one, in the latter its a normal Sum
   * semantic. This allows the counting of unique items.
   *
   */
  abstract
  class FeltCubeAggUniqueColSem extends FeltCubeAggPrimColSem {
    final override val semanticRt: FeltCubeAggUniqueSemRt = FeltCubeAggUniqueSemRt()
  }

  /**
   * Your classic max aggregation
   *
   */
  abstract
  class FeltCubeAggMaxColSem extends FeltCubeAggPrimColSem {
    final override val semanticRt: FeltCubeAggMaxSemRt = FeltCubeAggMaxSemRt()
  }

  /**
   * Your classic min aggregation
   *
   */
  abstract
  class FeltCubeAggMinColSem extends FeltCubeAggPrimColSem {
    final override val semanticRt: FeltCubeAggMinSemRt = FeltCubeAggMinSemRt()
  }

}
