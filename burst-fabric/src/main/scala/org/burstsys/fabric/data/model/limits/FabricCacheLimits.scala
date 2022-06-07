/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.limits

import java.util.concurrent.TimeUnit

import org.burstsys.fabric.configuration._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * A helper class to support the setting of important resource high/low limits for cache. Used to direct
 * resource management e.g. evict from memory, flush from disk.
 * Separated out for unit tests.
 */
trait FabricSnapCacheLimits extends Any {

  /**
   * how long to wait before starting resource tender
   *
   * @return
   */
  def tendStartWait: Duration = 30 seconds

  /**
   * how often to tend the cache
   *
   * @return
   */
  def tendPeriod: Duration = Duration(burstFabricCacheTendMinutesProperty.getOrThrow, TimeUnit.MINUTES)

  /**
   * is memory usage below the low water mark
   *
   * @return
   */
  def memoryUsageBelowLowWater: Boolean

  /**
   * is memory usage above the high water mark
   *
   * @return
   */
  def memoryUsageAboveHighWater: Boolean

  /**
   * is disk usage below the low water mark
   *
   * @return
   */
  def diskUsageBelowLowWater: Boolean

  /**
   * is disk usage above the high water mark
   *
   * @return
   */
  def diskUsageAboveHighWater: Boolean

  /**
   * make sure we have adequate resources to start up the cache (for now just prints out current resources)
   */
  def validateResourceMinimums(): Unit = {}

}

final case
class FabricSnapCachePropertyLimits() extends AnyRef with FabricSnapCacheLimits {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  private val highWaterMemoryMark: Double = burstFabricCacheMemoryHighMarkPercentProperty.getOrThrow
  private val lowWaterMemoryMark: Double = burstFabricCacheMemoryLowMarkPercentProperty.getOrThrow
  private val highWaterDiskMark: Double = burstFabricCacheDiskHighMarkPercentProperty.getOrThrow
  private val lowWaterDiskMark: Double = burstFabricCacheDiskLowMarkPercentProperty.getOrThrow

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def validateResourceMinimums(): Unit = {
    log info s"CACHE_MEMORY_RESOURCE highWaterMemoryMark=$highWaterMemoryMark (%), lowWaterMemoryMark=$lowWaterMemoryMark (%)"
    log info s"CACHE_DISK_RESOURCE highWaterDiskMark=$highWaterDiskMark (%), lowWaterDiskMark=$lowWaterDiskMark (%)"
  }

  override
  def memoryUsageBelowLowWater: Boolean = memoryPercentUsed < lowWaterMemoryMark

  override
  def memoryUsageAboveHighWater: Boolean = memoryPercentUsed > highWaterMemoryMark

  override
  def diskUsageBelowLowWater: Boolean = diskPercentUsed < lowWaterDiskMark

  override
  def diskUsageAboveHighWater: Boolean = diskPercentUsed > highWaterDiskMark

}
