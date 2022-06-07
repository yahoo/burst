/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache.slice

import org.burstsys.agent.api.BurstQueryGenerationState
import org.burstsys.agent.api.BurstQueryGenerationState._
import org.burstsys.fabric.data.model.slice.state._

import scala.language.implicitConversions

package object state {

  implicit def fabricToThriftGenerationState(state: FabricDataState): BurstQueryGenerationState = state match {
    case FabricDataWarm => Warm
    case FabricDataNoData => NoData
    case FabricDataHot => Hot
    case FabricDataMixed => Mixed
    case FabricDataCold => Cold
    case FabricDataFailed => Failed
    case _ => ???
  }

}
