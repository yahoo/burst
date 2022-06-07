/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.pump

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.fabric.configuration.{burstFabricCacheImpellersProperty, cacheSpindleFolders}
import org.burstsys.fabric.data.model.slice.region.FabricRegionTag
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import org.burstsys.vitals.logging._

/**
 * The intake dispatches writes to the appropriate region's impeller for eventual write processing.
 */
object FabricCacheIntake extends VitalsService {

  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = s"fabric-cache-intake"

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Pumping
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[pump]
  final val impellerIdGenerator = new AtomicInteger

  type ImpellerSet = ArrayBuffer[FabricCacheImpeller]

  private[this]
  final val _impellerMap = new ConcurrentHashMap[FabricRegionTag, ImpellerSet].asScala

  /**
   * dispatch to a random instance of a worker for the appropriate region tag
   *
   * @param regionTag
   */
  final
  def assignImpeller(regionTag: String): FabricCacheImpeller = {
    val regionImpellers = _impellerMap(regionTag)
    regionImpellers(math.abs(Random.nextInt) % regionImpellers.length)
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
      var spindleId = 0
      val spindleCount = cacheSpindleFolders.length
      val totalImpellers = burstFabricCacheImpellersProperty.getOrThrow * spindleCount
      for (_ <- 0 until totalImpellers) {
        val folder = cacheSpindleFolders(spindleId) // round robin through spindles
        val impellerId = impellerIdGenerator.getAndIncrement
        _impellerMap.getOrElseUpdate(folder, new ImpellerSet) += FabricCacheImpeller(spindleId, impellerId, folder).start
        spindleId = (spindleId + 1) % spindleCount // wrap around if necessary
      }
      log info burstStdMsg(s"spun up $totalImpellers impeller threads for ${spindleCount} spindle(s)")
      markRunning
      this
    }
  }

  final override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      _impellerMap.values.flatten.foreach(_.stop)
      _impellerMap.clear()
      impellerIdGenerator.set(0)
      markNotRunning
      this
    }
  }

}
