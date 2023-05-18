/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store

import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.alloy.store.exceptional.FailureMode.{DefaultFailureRate, OnWorker, UncaughtException}
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.properties._

package object exceptional extends VitalsLogger {

  final val ExceptionalStoreName = "exceptional"

  final case class StoreFailureMode(location: String, failure: String, rate: Double, failingContainers: Array[Long] = Array.empty)

  object StoreFailureMode {
    def apply(datasource: FabricDatasource): StoreFailureMode = {
      val properties = datasource.view.storeProperties.extend
      StoreFailureMode(
        properties.getValueOrDefault(FailureMode.FailureLocation, OnWorker),
        properties.getValueOrDefault(FailureMode.FailureMode, UncaughtException),
        properties.getValueOrDefault(FailureMode.FailureRate, DefaultFailureRate),
        properties.getValueOrDefault(FailureMode.FailingContainers, Array.emptyLongArray)
      )
    }
  }

  object FailureMode {
    val FailureLocation = "burst.store.exception.FailureLocation"
    val OnWorker = "FailOnWorker"
    val OnSupervisor = "FailOnSupervisor"

    val FailureMode = "burst.store.exception.FailureMode"
    val UncaughtException = "RuntimeException"
    val FabricException = "FabricException"
    val StoreTimeout = "StoreTimeout"
    val NoData = "NoData"

    val FailureRate = "burst.store.exception.FailureRate"
    val DefaultFailureRate: Double = 0.5

    val FailingContainers = "burst.store.exception.FailingContainers"
  }

}
