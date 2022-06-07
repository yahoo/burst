/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.exceptional.worker

import org.burstsys.alloy.store.exceptional
import org.burstsys.alloy.store.exceptional.log
import org.burstsys.alloy.store.exceptional.ExceptionalSlice
import org.burstsys.alloy.store.exceptional.FailureMode
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.FabricDataException
import org.burstsys.fabric.data.model.slice.state.FabricDataNoData
import org.burstsys.fabric.data.model.slice.state.FabricDataState
import org.burstsys.fabric.data.model.slice.state.FabricDataWarm
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.data.worker.store.FabricStoreWorker
import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
import org.burstsys.tesla

import java.util.concurrent.TimeUnit
import scala.util.Random

final case
class ExceptionalStoreWorker(container: FabricWorkerContainer) extends FabricStoreWorker with FabricWorkerLoader {

  override protected def initializeSlice(snap: FabricSnap): FabricDataState = {
    val slice = snap.slice.asInstanceOf[ExceptionalSlice]
    slice match {
      case exception: ExceptionalSlice =>
        try {
          snap.data.openForWrites()
          val failureMode = exception.failureMode
          if (failureMode.location == FailureMode.OnWorker) {
            val byChance = failureMode.rate >= Random.nextDouble()
            val byContainer = container.containerId.exists(failureMode.failingContainers.contains(_))
            failureMode.failure match {
              case FailureMode.NoData =>
                snap.metadata.state = FabricDataNoData
                snap.metadata.generationMetrics.recordSliceEmptyColdLoad(loadTookMs = 0, regionCount = -1) // TODO what should this be?
                return FabricDataNoData

              case FailureMode.UncaughtException =>
                if (byChance || byContainer)
                  throw new RuntimeException("Heads you lose, tails I win")

              case FailureMode.FabricException =>
                if (byChance || byContainer)
                  throw new FabricDataException("Heads you lose, tails I win")

              case FailureMode.StoreTimeout =>
                if (byChance || byContainer)
                  Thread.sleep(TimeUnit.MINUTES.toMillis(11)) // wave timeouts are currently hard coded, we should parameterize them soon

              case _ => // ignore other failure modes on the worker
            }
          }

          snap.data.queueParcelForWrite(tesla.parcel.factory.grabParcel(1028))
          snap.metadata.state = FabricDataWarm
          FabricDataWarm
        } finally {
          snap.data.waitForWritesToComplete()
          snap.data.closeForWrites()
        }

      case _ => throw new IllegalArgumentException("Slice was not exceptional. Now it is.")
    }
  }

  /**
   * the well known store name - used for registry/lookup
   *
   * @return
   */
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
