/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker.cache.bench
//
//import org.burstsys.fabric.configuration
//import org.burstsys.fabric.data.model.slice.state.{FabricDataWarm, FabricDataState}
//import org.burstsys.fabric.data.model.slice.{FabricGenerationHash, FabricSlice, FabricSliceKey}
//import org.burstsys.fabric.data.model.snap.FabricSnap
//import org.burstsys.fabric.data.model.slice.data.FabricSliceData
//import org.burstsys.fabric.data.worker.store.FabricWorkerLoader
//import org.burstsys.fabric.metadata.model
//import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
//import org.burstsys.fabric.topology.model.node.UnknownFabricNodeId
//import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
//import org.burstsys.schema.unity.BurstUnityMockData
//import org.burstsys.tesla
//import org.burstsys.tesla.parcel.TeslaParcel
//import org.burstsys.tesla.thread.request._
//import org.burstsys.vitals.errors.VitalsException
//import org.burstsys.vitals.errors._
//import org.burstsys.vitals.instrument._
//import org.burstsys.vitals.net.getPublicHostAddress
//import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}
//
//import scala.concurrent.duration._
//import scala.concurrent.{Future, Promise}
//import scala.language.postfixOps
//import scala.util.{Failure, Success}
//import org.burstsys.vitals.logging._
//
///**
// * == purpose ==
// * when the cache first starts up, or other times perhaps, this class allows for a benchmark to be run and its results captures
// * for logging or other usage. This is so we have a upper bound for write speeds to evaluate relative and absolute
// * performance metrics and perhaps fine tune parameters.
// * <p/>
// * == algorithm ==
// * <ol>
// * <li>Create a test set of '''inflated''' parcels from a mock data source (no inflation time is required)<li>
// * </ol>
// */
//trait FabricCacheBench {
//
//  /**
//   * run a cache disk write benchmark and return the results as a human
//   */
//  def benchmark(): Future[FabricCacheBenchmark]
//
//  /**
//   * available region spindle folders
//   *
//   * @return
//   */
//  def spindleCount: Int
//
//  def targetByteSize: Long
//}
//
//object FabricCacheBench {
//
//  // 1 073 741 824
//  final def apply(sliceByteSize: Long = 1 * GB): FabricCacheBench =
//    FabricCacheBenchContext(sliceByteSize: Long)
//
//  final def spawnCacheBenchmark: Future[FabricCacheBenchmark] = {
//    val promise = Promise[FabricCacheBenchmark]
//    TeslaRequestFuture {
//      Thread.sleep((5 minute).toMillis)
//      FabricCacheBench(sliceByteSize = 1 * GB).benchmark() onComplete {
//        case Failure(t) => log error burstStdMsg(s"cache benchmark failed $t", t)
//          promise.failure(t)
//        case Success(result) =>
//          promise.success(result)
//      }
//    }
//    promise.future
//  }
//
//}
//
//private final case
//class FabricCacheBenchContext(targetByteSize: Long) extends FabricCacheBench with FabricWorkerLoader {
//
//  override def spindleCount: Int = _spindleFolders.length
//
//  private val _spindleFolders: Array[String] = configuration.cacheSpindleFolders
//
//  private var data: Array[TeslaParcel] = _
//
//  private val bytesPerItem = 1200
//
//  private val datasource: FabricDatasource = FabricDatasource()
//
//  override
//  def benchmark(): Future[FabricCacheBenchmark] = {
//    val guid = newBurstUid
//    val sliceHash = newBurstUid
//    val sliceKey = 0
//    val tag = s"FabricCacheBench.benchmark(guid=$guid, targetByteSize=$targetByteSize)"
//    val promise = Promise[FabricCacheBenchmark]
//
//    TeslaRequestFuture {
//      val itemCount = targetByteSize.toInt / bytesPerItem
//
//      log info burstStdMsg(s"$tag PRESS_DATA itemCount=$itemCount")
//
//      data = BurstUnityMockData(itemCount).pressToInflatedParcels
//
//      // set them off
//      log info burstStdMsg(s"$tag SPAWN_WRITER for $spindleCount spindle(s)")
//      val start = System.nanoTime()
//      val futureSlice = TeslaRequestFuture(writeExecute(guid, sliceKey, sliceHash))
//
//      // do stuff with the results as they come in
//      futureSlice onComplete {
//        case Failure(t) =>
//          log info burstStdMsg(s"$tag  FAILURE $t", t)
//          promise.failure(t)
//          data foreach tesla.parcel.factory.releaseParcel
//        case Success(slice) =>
//          log info burstStdMsg(s"$tag  SUCCESS")
//          promise.success(
//            FabricCacheBenchmark(
//              targetBytesSize = targetByteSize,
//              actualBytesSize = ???, //slice.metadata.generationMetrics.byteCount,
//              elapsedNs = System.nanoTime - start
//            )
//          )
//          data foreach tesla.parcel.factory.releaseParcel
//      }
//    }
//    promise.future
//  }
//
//  private def writeExecute(guid: VitalsUid, sliceKey: FabricSliceKey, sliceHash: FabricGenerationHash): FabricSliceData = {
//    val tag = s"FabricCacheBench.writeExecute(guid=$guid)"
//    log info tag
//    val slice = FabricSlice(
//      guid = guid,
//      datasource = datasource,
//      worker = FabricWorkerNode(UnknownFabricNodeId, workerNodeAddress = getPublicHostAddress),
//      sliceKey = 0,
//      generationHash = sliceHash,
//      slices = 1
//    )
//    try {
////      this.fetchSliceFromCacheOrInitialize(slice)
//      ???
//    } catch safely {
//      case t: Throwable =>
//        val msg = burstStdMsg(t)
//        log.error(msg, t)
//        throw VitalsException(msg, t)
//    }
//  }
//
//  override protected
//  def initializeSlice(snap: FabricSnap): FabricDataState = {
//    val tag = s"FabricCacheBench.initializeSlice(${snap})"
//    log info tag
//    val start = System.currentTimeMillis()
//    try {
//      snap.data.openForWrites()
//      try {
//        var itemCount = 0
//        var byteCount = 0
//        data foreach {
//          parcel =>
//            itemCount += 1
//            byteCount += parcel.currentUsedMemory
//            snap.data queueParcelForWrite parcel
//        }
//        snap.data.waitForWritesToComplete()
//
//        snap.metadata.generationMetrics.recordSliceNormalColdLoad(
//          loadTookMs = ((System.nanoTime - start) / 1e6).toLong, itemCount = itemCount,
//          regionCount = snap.data.regionCount, potentialItemCount = itemCount,
//          rejectedItemCount = 0,
//          byteCount = byteCount
//        )
//        snap.metadata.state = FabricDataWarm
//      } finally snap.data.closeForWrites()
//    } catch safely {
//      case t: Throwable =>
//        val msg = burstStdMsg(t)
//        log.error(msg, t)
//        throw VitalsException(msg, t)
//    }
//    snap.metadata.state
//  }
//
//}
