/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.pump

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import org.burstsys.fabric.wave.data.model.slice.region.FabricRegionTag
import org.burstsys.fabric.wave.data.model.slice.region.writer.FabricRegionWriter
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsPojo, VitalsServiceModality}
import org.burstsys.vitals.errors._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import org.burstsys.vitals.logging._

/**
 * There are a fixed set of impellers assigned to a one disk spindle's worth of write IO.
 * [[FabricRegionWriter]] instances are assigned to an impellers and vector all their write IO to a
 * single impeller queue. This is important in order to guarantee ordering (for free'ing of descriptors)
 */
trait FabricCacheImpeller extends VitalsService {

  /**
   * a unique id for this impeller instance
   *
   * @return
   */
  def impellerId: FabricImpellerId

  /**
   * region of disk that this impeller covers (presumably a folder on a spindle though it doesn't have to be)
   *
   */
  def regionTag: FabricRegionTag

  /**
   * pass the region writer to the impeller. Only one item will be processed from the queue at a time.
   *
   */
  def impel(writer: FabricRegionWriter): Unit

}

object FabricCacheImpeller {

  def apply(
             spindleId: FabricSpindleId,
             impellerId: FabricImpellerId,
             regionTag: FabricRegionTag
           ): FabricCacheImpeller =
    FabricCacheImpellerContext(
      spindleId,
      impellerId,
      regionTag
    )

}

private final case
class FabricCacheImpellerContext(
                                  spindleId: FabricSpindleId,
                                  impellerId: FabricImpellerId,
                                  regionTag: FabricRegionTag) extends VitalsService with FabricCacheImpeller {

  override def modality: VitalsServiceModality = VitalsPojo

  override def serviceName: String = s"fabric-cache-impeller(spindleId=$spindleId, impellerId=$impellerId, regionTag=$regionTag)"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _running = new AtomicBoolean()

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    _running.set(true)
    spawnWorker
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    _running.set(false)
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Processing
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _writerQueue = new ArrayBlockingQueue[FabricRegionWriter](1e5.toInt)

  private val writeQueueTimeout: Duration = 10 seconds

  /**
   * This writer has work to do, put it on a queue for a worker thread
   */
  def impel(writer: FabricRegionWriter): Unit = {
    val tag = s"FabricCacheImpeller.impel(regionTag=$regionTag)"
    var continue = true
    while (continue) {
      if (_writerQueue.offer(writer, writeQueueTimeout.toNanos, TimeUnit.NANOSECONDS)) {
        continue = false
      } else {
        log warn burstStdMsg(s"WRITE_QUEUE_TIMEOUT after $writeQueueTimeout $tag")
      }
    }
  }

  /**
   * a fixed set of worker threads that each handle one disk write at a time to the spindle region
   *
   * @return
   */
  private[this]
  def spawnWorker: Future[Unit] = {
    val tag = s"FabricCacheImpeller.spawnWorker(regionTag=$regionTag)"
    TeslaRequestFuture {
      val name = Thread.currentThread.getName
      Thread.currentThread.setName(f"fab-impeller-$impellerId%02d")
      try {
        while (_running.get) {
          val writer = _writerQueue.poll(writerQueuePoolWait.toMillis, TimeUnit.MILLISECONDS)
          if (writer != null) TeslaWorkerCoupler(writer.writeNextParcel)
        }
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"IMPELLER_THREAD_DIED!!! $tag", t)
      } finally {
        Thread.currentThread.setName(name)
      }
    }
  }

}
