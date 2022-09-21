/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.apache.commons.io.FileUtils
import org.burstsys.fabric
import org.burstsys.fabric.metadata.ViewCacheEraseTtlMsProperty
import org.burstsys.fabric.metadata.ViewCacheEvictTtlMsProperty
import org.burstsys.fabric.metadata.ViewCacheFlushTtlMsProperty
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.tesla.configuration.burstTeslaWorkerThreadCountProperty
import org.burstsys.vitals.io.GB
import org.burstsys.vitals.net.VitalsHostAddress
import org.burstsys.vitals.net.VitalsHostPort
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import java.lang.Runtime.getRuntime
import java.nio.file.Paths
import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  def configureForUnitTests(): Unit = {
    burstFabricCacheImpellersProperty.set(2)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // THREADING/CONCURRENCY LIMITS
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * maximum number of concurrent waves allowed through the master
   */
  val burstFabricWaveConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.wave.concurrency",
    description = "max allowed concurrent waves",
    default = Some(12) // for now...
  )

  val burstFabricColdLoadConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.cache.load.concurrency",
    description = "max concurrent cold loads in worker cache",
    default = Some(3) // for now...
  )

  val burstFabricScanConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.cache.scan.concurrency",
    description = "max concurrent scans in worker engine",
    default = Some(12) // for now...
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NETWORK
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstFabricNetHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.fabric.net.host",
    description = "host/address for fabric net",
    default = Some(getPublicHostAddress)
  )

  val burstFabricNetPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.fabric.net.port",
    description = "port for fabric net",
    default = Some(37060)
  )

  /**
   * currently not used... NETTY defaults...
   */
  val burstFabricNetClientThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.net.client.threads",
    description = "",
    default = Some(getRuntime.availableProcessors)
  )

  /**
   * currently not used... NETTY defaults...
   */
  val burstFabricNetServerThreadsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.net.server.threads",
    description = "",
    default = Some(getRuntime.availableProcessors * 2)
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TOPOLOGY
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstFabricTopologyHomogeneous: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.topology.homogeneous",
    description = "whether or not the topology should consist of only homogeneous versions",
    default = Some(true)
  )

  val burstFabricHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.fabric.master.host",
    description = "host/address for fabric master",
    default = Some(getPublicHostAddress)
  )

  val burstFabricMonikerProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.fabric.moniker",
    description = "moniker of this node as registered in the catalog",
    default = Some(getPublicHostName)
  )

  val burstFabricPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.fabric.master.port",
    description = "port for fabric master",
    default = Some(37040)
  )

  val burstFabricMasterStandaloneProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.master.standalone",
    description = "enable standalone master container world",
    default = Some(false)
  )

  val burstFabricWorkerStandaloneProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.worker.standalone",
    description = "enable standalone worker container world",
    default = Some(false)
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REGIONS
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a '';'' separated list of spindle data folders
   */
  val burstFabricCacheSpindleFoldersProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.fabric.cache.spindles",
    description = "a ';' separated list of spindle dirs",
    default = Some(cacheRegionTmpFolders.mkString(";"))
  )

  def cacheSpindleFolders: Array[String] = {
    burstFabricCacheSpindleFoldersProperty.getOrThrow.split(";")
  }

  /**
   * The number of regions in a slice. In a slice each region is backed by a file. By default we set the number of
   * of regions equal to the number of worker threads, so that if a worker is scanning a slice it can use all available
   * worker threads to share in the processing. In the future this may be configurable, but it requires more testing
   * to verify what values would be reasonable.
   */
  val burstFabricCacheRegionCount: Int = burstTeslaWorkerThreadCountProperty.getOrThrow

  /**
   * the number of impellers per spindle. Basically this is how hard you want to hit disk IO on a single
   * physical disk's queue
   */
  val burstFabricCacheImpellersProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.fabric.cache.impellers",
    description = "how many impellers per spindle",
    default = Some(8)
  )

  /**
   * tmp folder version of cache for unit tests
   *
   * @return
   */
  private def cacheRegionTmpFolders: Array[String] = {
    (0 until 4).map {
      i =>
        val tmpFolder = Paths.get(FileUtils.getTempDirectoryPath, s"burst-cache$i")
        val tmpFile = tmpFolder.toAbsolutePath.toFile
        FileUtils.forceMkdir(tmpFile)
        FileUtils.cleanDirectory(tmpFile)
        tmpFolder.toAbsolutePath.toString

    }.toArray
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CACHE EVICT/FLUSH
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * flush (delete) all snap cache files when a worker boots
   */
  val burstFabricCacheBootFlushProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.fabric.cache.boot.flush",
    description = "do a boot flush of all slice generations",
    default = Some(true)
  )

  val burstFabricCacheTendMinutesProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.fabric.cache.tend.period.minutes",
    description = "how often to tend the cache",
    default = Some((5 minutes).toMinutes)
  )

  /**
   * percentage of memory used where evictions starts
   */
  val burstFabricCacheMemoryHighMarkPercentProperty: VitalsPropertySpecification[Double] = VitalsPropertySpecification[Double](
    key = "burst.fabric.cache.memory.high.percent",
    description = "high water mark memory usage percentage...",
    default = Some(40.0)
  )

  /**
   * percentage of memory used where evictions stops
   */
  val burstFabricCacheMemoryLowMarkPercentProperty: VitalsPropertySpecification[Double] = VitalsPropertySpecification[Double](
    key = "burst.fabric.cache.memory.low.percent",
    description = "low water mark memory usage percentage...",
    default = Some(25.0)
  )

  /**
   * percentage of disk used where flushing starts
   */
  val burstFabricCacheDiskHighMarkPercentProperty: VitalsPropertySpecification[Double] = VitalsPropertySpecification[Double](
    key = "burst.fabric.cache.disk.high.percent",
    description = "high water mark disk usage percentage...",
    default = Some(60.0)
  )

  /**
   * percentage of disk used where flushing stops
   */
  val burstFabricCacheDiskLowMarkPercentProperty: VitalsPropertySpecification[Double] = VitalsPropertySpecification[Double](
    key = "burst.fabric.cache.disk.low.percent",
    description = "low water mark disk usage percentage...",
    default = Some(50.0)
  )

  /**
   * @see [[org.burstsys.fabric.metadata.ViewCacheEvictTtlMsProperty]]
   *      how many ms before a cached dataset is considered for eviction
   */
  val burstViewCacheEvictTtlMsPropertyDefault: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = ViewCacheEvictTtlMsProperty,
    description = "ms after last access cache is triggered to evict (after read/write)",
    default = Some((15 minutes).toMillis)
  )

  /**
   * @see [[org.burstsys.fabric.metadata.ViewCacheFlushTtlMsProperty]]
   *      how many ms before a cached dataset is considered for flushing
   */
  val burstViewCacheFlushTtlMsPropertyDefault: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = ViewCacheFlushTtlMsProperty,
    description = "ms after last access cache is triggered to flush (after evict)",
    default = Some((2 hours).toMillis)
  )

  /**
   * @see [[org.burstsys.fabric.metadata.ViewCacheEraseTtlMsProperty]]
   *      how many ms before a cached dataset is considered for eraseing
   */
  val burstViewCacheEraseTtlMsPropertyDefault: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = ViewCacheEraseTtlMsProperty,
    description = "ms after last access cache is triggered to erase (after flush)",
    default = Some((30 minutes).toMillis)
  )

  final def evictTtlMsFromDatasource(datasource: FabricDatasource): Long =
    datasource.view.viewProperties.extend.getValueOrDefault(
      metadata.ViewCacheEvictTtlMsProperty, burstViewCacheEvictTtlMsPropertyDefault.getOrThrow
    )

  final def flushTtlMsFromDatasource(datasource: FabricDatasource): Long =
    datasource.view.viewProperties.extend.getValueOrDefault(
      metadata.ViewCacheFlushTtlMsProperty, burstViewCacheFlushTtlMsPropertyDefault.getOrThrow
    )

  final def eraseTtlMsFromDatasource(datasource: FabricDatasource): Long =
    datasource.view.viewProperties.extend.getValueOrDefault(
      metadata.ViewCacheEraseTtlMsProperty, burstViewCacheEraseTtlMsPropertyDefault.getOrThrow
    )

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Datasources
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstFabricDatasourceMaxSizeProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.fabric.datasource.max.size.bytes",
    description = "the maximum desired size of a loaded dataset. Datasets that exceed this size will have the `loadInvalid` flag set in their generation metrics",
    default = Some(100 * GB)
  )

  final def maxDatasetSizeFromDatasource(datasource: FabricDatasource): Long =
    datasource.view.viewProperties.extend.getValueOrDefault(
      metadata.ViewNextDatasetSizeMaxProperty, burstFabricDatasourceMaxSizeProperty.getOrThrow
    )

}
