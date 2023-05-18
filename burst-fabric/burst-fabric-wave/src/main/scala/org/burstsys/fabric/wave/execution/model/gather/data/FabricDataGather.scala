/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.data

import org.burstsys.fabric.wave.execution.model.gather.{FabricGather, FabricGatherContext}

/**
 * all [[FabricGather]] that return some form of data (including no data [[FabricEmptyGather]])
 */
trait FabricDataGather extends AnyRef with FabricGather {

  /**
   * the number of rows returned by this gather
   *
   * @return
   */
  def rowCount: Long

  /**
   * the number of queries that overflowed returned by this gather
   *
   * @return
   */
  def overflowCount: Long

  /**
   * the number of queries that limited returned by this gather
   *
   * @return
   */
  def limitCount: Long

  /**
   * the number of queries  returned by this gather
   *
   * @return
   */
  def queryCount: Long

  /**
   * the number of queries that succeeded returned by this gather
   *
   * @return
   */
  def successCount: Long

}

/**
 * abstract base class for all data gathers
 */
abstract
class FabricDataGatherContext extends FabricGatherContext with FabricDataGather {

  override def sliceFinalize(): Unit = {
  }

  override def waveFinalize(): Unit = {
  }

}
