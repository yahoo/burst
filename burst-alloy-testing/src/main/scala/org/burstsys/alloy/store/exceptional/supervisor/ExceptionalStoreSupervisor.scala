/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.exceptional.supervisor

import org.burstsys.alloy.store.exceptional
import org.burstsys.alloy.store.exceptional.{ExceptionalSlice, FailureMode, StoreFailureMode, log}
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.data.supervisor.store.FabricStoreSupervisor
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.uid._

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.util.Random

final case class ExceptionalStoreSupervisor(container: FabricSupervisorContainer) extends FabricStoreSupervisor {

  override
  def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    TeslaRequestFuture {
      val sliceCount = workers.length
      val failureMode = StoreFailureMode(datasource)
      if (failureMode.location == FailureMode.OnSupervisor) {
        val byChance = failureMode.rate >= Random.nextDouble()
        val byContainer = container.containerId.exists(failureMode.failingContainers.contains(_))
        failureMode.failure match {
          case FailureMode.UncaughtException =>
            if (byChance || byContainer)
              throw VitalsException("Heads you lose, tails I win")

          case FailureMode.StoreTimeout =>
            if (byChance || byContainer)
              Thread.sleep(TimeUnit.MINUTES.toMillis(11)) // wave timeouts are currently hard coded, we should parameterize them soon

          case _ => // ignore other failure modes on the supervisor
        }
      }

      workers.indices.map {
        sliceKey =>
          ExceptionalSlice(guid, sliceKey, sliceKey.toString, sliceCount, datasource, worker = workers(sliceKey), failureMode)
      }.toArray
    }
  }

  override def storeName: FabricStoreName = exceptional.ExceptionalStoreName

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

}
