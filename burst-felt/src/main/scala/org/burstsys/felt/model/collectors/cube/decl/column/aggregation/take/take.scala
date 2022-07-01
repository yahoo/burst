/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.aggregation

package object take {

  /**
   * sort and truncate the result set so that only the top 'count' rows with this field maximized are returned
   */
  object FeltCubeTopTakeSemMode extends FeltCubeTakeSemMode(0)

  /**
   * reverse sort and truncate the result set so that only the bottom 'count' rows with this field maximized are returned
   */
  object FeltCubeBottomTakeSemMode extends FeltCubeTakeSemMode(1)

  /**
   * a semantic that supports sorting and truncating based on an aggregate value
   * i.e. ''top k''
   */
  abstract
  class FeltCubeAggTakeColSem extends FeltCubeAggColSem {

    /**
     * the mode e.g. top, bottom...
     */
    def mode: FeltCubeTakeSemMode

    /**
     * the final sort/truncate operation on the master
     *
     * @return
     */
    def scatterK: Int

    /**
     * the sort/truncate operations on each worker node slice. If set to -1, then this defaults
     * to 10 * finalK.
     *
     * @return
     */
    def sliceK: Int = -1

    /**
     * the sort/truncate operations on each item blob . If set to -1, then this defaults
     * *                 to 10 * partitionK.
     *
     * @return
     */
    def itemK: Int = -1

    final override lazy val semanticRt: FeltCubeAggTakeSemRt =
      FeltCubeAggTakeSemRt(_mode = mode, _scatterK = scatterK, _sliceK = sliceK, _itemK = itemK)
  }

  /**
   * A top semantic is an aggregation that controls the ultimate sorting and truncation of a result set so that only
   * the top 'count' rows with this field maximized are returned. This is a traditional topK semantic within a single
   * item being traversed, however globally this is what we call a 'pseudo topK' because only a full sort across all
   * rows would provide that - and that is prohibitively expensive for little real value in our applications.
   *
   */
  abstract
  class FeltCubeAggTopColSem extends FeltCubeAggTakeColSem {
    final override val mode: FeltCubeTakeSemMode = FeltCubeTopTakeSemMode
  }

  /**
   *
   */
  abstract
  class FeltCubeAggBottomColSem extends FeltCubeAggTakeColSem {
    final override val mode: FeltCubeTakeSemMode = FeltCubeBottomTakeSemMode
  }

}
