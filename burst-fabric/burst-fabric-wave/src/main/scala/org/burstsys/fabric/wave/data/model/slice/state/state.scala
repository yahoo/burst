/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice

package object state {

  sealed case class FabricDataState(label: String) {
    override def toString: String = label
  }

  /**
   * this part of the data is not in memory but we do not know if it's on disc
   */
  object FabricDataCold extends FabricDataState("Cold")

  /**
   * this part of the data has valid region disc images
   */
  object FabricDataWarm extends FabricDataState("Warm")

  /**
   * this part of the data is 'empty'
   */
  object FabricDataNoData extends FabricDataState("NoData")

  /**
   * this part of the data is in memory
   */
  object FabricDataHot extends FabricDataState("Hot")

  /**
   * this part of the data is in more than one state
   */
  object FabricDataMixed extends FabricDataState("Mixed")

  /**
   * this part of the data is in a failed state
   */
  object FabricDataFailed extends FabricDataState("Failed")

}
