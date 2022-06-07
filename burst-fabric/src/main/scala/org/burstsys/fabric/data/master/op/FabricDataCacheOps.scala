/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.master.op

import org.burstsys.fabric.data.master.FabricMasterDataContext
import org.burstsys.fabric.data.model.generation.{FabricGeneration, FabricGenerationIdentity}
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops._
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla
import org.burstsys.tesla.scatter.slot.TeslaScatterSlotSucceed
import org.burstsys.tesla.scatter.{TeslaScatter, TeslaScatterBegin, TeslaScatterSucceed}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * the cache operational aspect of the master side data service
 */
trait FabricDataCacheOps extends FabricCacheOps {

  self: FabricMasterDataContext =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def cacheGenerationOp(
                         guid: VitalsUid,
                         operation: FabricCacheManageOp,
                         generationKey: FabricGenerationKey,
                         parameters: Option[Seq[FabricCacheOpParameter]]
                       ): Future[Seq[FabricGeneration]] = {
    lazy val tag = s"FabricDataCacheOps.cacheGenerationOp"
    val promise = Promise[Seq[FabricGeneration]]
    TeslaRequestFuture {
      val scatter = tesla.scatter.pool.grabScatter(guid)
      try {
        val results = new ArrayBuffer[FabricGeneration]

        container.topology.healthyWorkers.foreach {
          w => scatter.addRequestSlot(CacheManageOperation(data = this, worker = w, operation = operation, generationKey = generationKey))
        }

        scatter.execute()
        process(scatter, results)

        val result = operation match {
          case FabricCacheSearch => filteredGenerations(collectGenerations(results), parameters)
          case FabricCacheEvict => collectGenerations(results)
          case FabricCacheFlush => collectGenerations(results)
          case _ => ???
        }

        promise.success(result)
      } catch safely {
        case t: Throwable => promise.failure(t)
      } finally tesla.scatter.pool releaseScatter scatter
    }
    promise.future
  }

  final override
  def cacheSliceOp(guid: VitalsUid, generationKey: FabricGenerationKey): Future[Seq[FabricSliceMetadata]] = {
    lazy val tag = s"FabricDataCacheOps.cacheSliceOp"
    val promise = Promise[Seq[FabricSliceMetadata]]
    TeslaRequestFuture {
      val scatter = tesla.scatter.pool.grabScatter(guid)
      try {
        val results = new ArrayBuffer[FabricSliceMetadata]

        container.topology.healthyWorkers.foreach {
          w => scatter.addRequestSlot(SliceFetchOperation(this, w, generationKey))
        }

        scatter.execute()
        process(scatter, results)
        promise.success(results)
      } finally tesla.scatter.pool releaseScatter scatter
    }
    promise.future
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private
  def process[T](scatter: TeslaScatter, collector: ArrayBuffer[T]): Unit = {
    lazy val tag = s"FabricDataCacheOps.process"
    while (true) {
      scatter.nextUpdate(10 minutes) match {
        case update: TeslaScatterSlotSucceed =>
          collector ++= update.slot.request.result.asInstanceOf[Seq[T]]

        case _: TeslaScatterSucceed =>
          return

        case update: TeslaScatterBegin =>

        case update =>
          throw VitalsException(s"$tag update=$update not handled")
      }
    }
  }

  final private
  def collectGenerations(newGenerations: ArrayBuffer[FabricGeneration]): Seq[FabricGeneration] = {
    lazy val tag = s"FabricDataCacheOps.collectGenerations (newGenerations=${newGenerations.size})"
    try {
      val resultMap = new mutable.HashMap[FabricGenerationIdentity, FabricGeneration]
      newGenerations.foreach {
        newGeneration =>
          resultMap.get(newGeneration.datasource.view) match {
            case None =>
              log info s"FAB_CACHE_COLLECT_NEW ${newGeneration.datasource.view} with ${newGeneration.slices.length} slice(s) $tag"
              resultMap += newGeneration.datasource.view -> newGeneration
            case Some(oldGeneration) =>
              log info s"FAB_CACHE_COLLECT_ADD ${newGeneration.datasource.view} with ${newGeneration.slices.length} slice(s) $tag"
              oldGeneration addSlices newGeneration.slices
          }
      }
      val results = resultMap.values
      results.foreach(_.finalizeMetrics())
      log info
        s"""|FAB_CACHE_COLLECT_RESULT
            |   generationCount=${results.size}
            |${results.mkString("", "\n", "")}
            |$tag""".stripMargin
      results.toSeq
    } catch safely {
      case t: Throwable => throw VitalsException(s"FAB_CACHE_COLLECT_FAIL $t $tag", t)
    }
  }

  final private
  def filteredGenerations(generations: Seq[FabricGeneration], params: Option[Seq[FabricCacheOpParameter]]): Seq[FabricGeneration] = {
    params
      .map { params => generations.filter { g => params.forall { f => compare(g, f) } } }
      .getOrElse(generations)
  }

  final private
  def compare(generation: FabricGeneration, parameter: FabricCacheOpParameter): Boolean = {
    val metrics = generation.generationMetrics
    parameter.name match {
      case FabricCacheByteCount => cmpLong(parameter.relation, metrics.byteCount, parameter.lVal)
      case FabricCacheItemCount => cmpLong(parameter.relation, metrics.itemCount, parameter.lVal)
      case FabricCacheSliceCount => cmpLong(parameter.relation, metrics.sliceCount, parameter.lVal)
      case FabricCacheRegionCount => cmpLong(parameter.relation, metrics.regionCount, parameter.lVal)
      case FabricCacheColdLoadAt => cmpLong(parameter.relation, metrics.coldLoadAt, parameter.lVal)
      case FabricCacheColdLoadTook => cmpLong(parameter.relation, metrics.coldLoadTook, parameter.lVal)
      case FabricCacheWarmLoadAt => cmpLong(parameter.relation, metrics.warmLoadAt, parameter.lVal)
      case FabricCacheWarmLoadTook => cmpLong(parameter.relation, metrics.warmLoadTook, parameter.lVal)
      case FabricCacheWarmLoadCount => cmpLong(parameter.relation, metrics.warmLoadCount, parameter.lVal)
      case FabricCacheSizeSkew => cmpDouble(parameter.relation, metrics.sizeSkew, parameter.dVal)
      case FabricCacheTimeSkew => cmpDouble(parameter.relation, metrics.timeSkew, parameter.dVal)
      case FabricCacheItemSize => cmpDouble(parameter.relation, metrics.itemSize, parameter.dVal)
      case FabricCacheItemVariation => cmpDouble(parameter.relation, metrics.itemVariation, parameter.dVal)
      case FabricCacheEarliestLoadAt => cmpLong(parameter.relation, metrics.earliestLoadAt, parameter.lVal)
      case FabricCacheLoadInvalid => cmpBoolean(parameter.relation, metrics.loadInvalid, parameter.bVal)
      case FabricCacheRejectedItemCount => cmpLong(parameter.relation, metrics.rejectedItemCount, parameter.lVal)
      case FabricCachePotentialItemCount => cmpLong(parameter.relation, metrics.potentialItemCount, parameter.lVal)
      case FabricCacheSuggestedSampleRate => cmpDouble(parameter.relation, metrics.suggestedSampleRate, parameter.dVal)
      case FabricCacheSuggestedSliceCount => cmpLong(parameter.relation, metrics.suggestedSliceCount, parameter.lVal)
      case _ => ???
    }
  }

  final private
  def cmpLong(relation: FabricCacheOpRelation, value: Long, test: Long): Boolean = {
    relation match {
      case FabricCacheLT => value < test
      case FabricCacheEQ => value == test
      case FabricCacheGT => value > test
      case _ => ???
    }
  }

  final private
  def cmpDouble(relation: FabricCacheOpRelation, value: Double, test: Double): Boolean = {
    relation match {
      case FabricCacheLT => value < test
      case FabricCacheEQ => value == test
      case FabricCacheGT => value > test
      case _ => ???
    }
  }

  final private
  def cmpBoolean(relation: FabricCacheOpRelation, value: Boolean, test: Boolean): Boolean = {
    relation match {
      case FabricCacheLT => false
      case FabricCacheEQ => value == test
      case FabricCacheGT => false
      case _ => ???
    }
  }
}

final private case
class CacheManageOperation(
                            data: FabricMasterDataContext,
                            worker: FabricWorkerNode,
                            operation: FabricCacheManageOp,
                            generationKey: FabricGenerationKey
                          ) extends CacheOpRequest[Seq[FabricGeneration]] {


  override def destinationHostName: VitalsHostName = worker.nodeName

  override
  def execute: Future[Unit] = {
    val guid = slot.scatter.guid
    lazy val tag = s"CacheManageOperation.execute(guid=$guid, ruid=$ruid, host=$destinationHostName)"
    val promise = Promise[Unit]()
    data.container.topology.getWorker(worker) match {
      case None =>
        promise.failure(VitalsException(s"$tag $worker not found").fillInStackTrace())
      case Some(w) =>
        w.connection.cacheManageOperation(guid, ruid, operation, generationKey) onComplete {
          case Success(r) =>
            promise.success((): Unit)
            result = r
            if (slot != null)
              slot.slotSuccess()
          case Failure(t) =>
            log error burstStdMsg(s"FAIL $t $tag", t)
            promise.failure(t)
            if (slot != null)
              slot.slotFailed(t)
        }
    }
    promise.future
  }

}

final private case
class SliceFetchOperation(
                           data: FabricMasterDataContext,
                           worker: FabricWorkerNode,
                           generationKey: FabricGenerationKey
                         ) extends CacheOpRequest[Seq[FabricSliceMetadata]] {

  override def destinationHostName: VitalsHostName = worker.nodeName

  override
  def execute: Future[Unit] = {
    val guid = slot.scatter.guid
    lazy val tag = s"CacheManageOperation.execute(guid=$guid, ruid=$ruid, host=$destinationHostName)"
    val promise = Promise[Unit]()
    data.container.topology.getWorker(worker) match {
      case None =>
        promise.failure(VitalsException(s"$tag $worker not found").fillInStackTrace())
      case Some(w) =>
        w.connection.cacheSliceFetchOperation(guid, ruid, generationKey) onComplete {
          case Success(r) =>
            promise.success((): Unit)
            result = r
            if (slot != null)
              slot.slotSuccess()
          case Failure(t) =>
            log error burstStdMsg(s"FAIL $t $tag", t)
            promise.failure(t)
            if (slot != null)
              slot.slotFailed(t)
        }
    }
    promise.future
  }
}
