/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.burnin

import io.opentelemetry.api.trace.{Span, TraceId}
import io.opentelemetry.context.Context
import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.fabric.wave.data.model.ops.FabricCacheFlush
import org.burstsys.fabric.wave.execution.model.result.status
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.supervisor.http.service.burnin.BurnInRunBatch.BurnInLabel
import org.burstsys.supervisor.http.service.provider.BurnInBatch.DurationType
import org.burstsys.supervisor.http.service.provider.BurnInDatasetDescriptor.LookupType
import org.burstsys.supervisor.http.service.provider.{BurnInBatch, BurnInDatasetDescriptor, BurnInEvent, BurnInLogEvent}
import org.burstsys.supervisor.trek.{BurnInDatasetTrek, BurnInQueryTrek}
import org.burstsys.tesla.thread.request.{TeslaRequestFuture, teslaRequestExecutor}
import org.burstsys.vitals
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.uid

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.{ConcurrentHashMap, ConcurrentSkipListMap, TimeoutException}
import java.util.logging.Level
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala
import scala.util.control.Breaks._
import scala.util.{Failure, Success, Try}

object BurnInRunBatch {
  val BurnInLabel = "Burn-In"

  /**
   * - look up views (w/associated domain) by pk/udk
   * - dedup domains by view
   * - create datasets for each view with negative pks
   */
  def apply(
             index: Int,
             batch: BurnInBatch,
           )(
             implicit
             agent: AgentService,
             catalog: CatalogService,
             registerEvent: BurnInEvent => Unit,
             shouldContinue: () => Boolean,
           ): BurnInRunBatch = {
    def logWarn(msg: String): Unit = registerEvent(BurnInLogEvent(msg, Level.WARNING))

    def logError(msg: String): Unit = registerEvent(BurnInLogEvent(msg, Level.SEVERE))

    val initialCounter = -1000 * (index + 1)

    val datasets = ArrayBuffer[BurnInRunDataset]()
    val counter = new AtomicLong(initialCounter)
    val domainDedupe = new ConcurrentSkipListMap[Long /* domain pk */ , ArrayBuffer[Long /* view pk */ ]]()
    val generated = mutable.Map[Long /* counter */ , BurnInDatasetDescriptor]()
    val loadQueries = mutable.Map[Long, String]()
    val loadIntervals = mutable.Map[Long, Option[Int]]()
    def nextGeneratedId(): Long = counter.getAndAdd(-1)

    /*
     * - look up views (w/associated domain) by pk/udk
     * - dedupe domains by view
     * - create datasets for each view with negative pks
     */

    for (dataset <- batch.datasets) {
      val views: Array[(Long, Long)] = dataset.lookupType match {
        case LookupType.ByPk =>
          catalog.findViewByPk(dataset.pk.get) match {
            case Failure(_) =>
              logWarn(s"Unable to find view by pk=${dataset.pk}")
              Array.empty
            case Success(view) =>
              Array((view.domainFk, view.pk))
          }
        case LookupType.ByUdk =>
          catalog.findViewByUdk(dataset.udk.get) match {
            case Failure(_) =>
              logWarn(s"Unable to find view by udk=${dataset.udk}")
              Array.empty
            case Success(view) =>
              Array((view.domainFk, view.pk))
          }
        case LookupType.ByProperty =>
          catalog.searchViewsByLabel(dataset.label.get, dataset.labelValue) match {
            case Failure(_) =>
              logWarn(s"Unable to find view by label=${dataset.label} value=${dataset.labelValue}")
              Array.empty
            case Success(views) =>
              views.map(v => (v.domainFk, v.pk))
          }
        case LookupType.Generate =>
          val domainPk = nextGeneratedId()
          val views = ArrayBuffer[(Long, Long)]()
          for (_ <- 0 until dataset.copies.getOrElse(1)) {
            generated(domainPk) = dataset
            views += ((domainPk, nextGeneratedId()))
          }
          views.toArray
      }

      for ((domainPk, viewPk) <- views) {
        val viewsForDomain = domainDedupe.computeIfAbsent(domainPk, _ => ArrayBuffer.empty[Long])
        loadIntervals(viewPk) = dataset.reloadEvery
        dataset.loadQuery.foreach(loadQueries(viewPk) = _)
        viewsForDomain += viewPk
      }
    }

    counter.set(initialCounter)
    for (domainWithViews <- domainDedupe.asScala) {
      val domainPk = domainWithViews._1
      val viewPks = domainWithViews._2

      val datasetId = nextGeneratedId()
      val (domain: FabricDomain, views: Array[FabricView]) = if (domainPk < 0) {
        val dataset = generated(domainPk)
        (dataset.domain.get.toFabric(datasetId), viewPks.map(_ => dataset.view.get.toFabric(nextGeneratedId(), datasetId)).toArray)
      } else {
        val domain = catalog.findDomainByPk(domainPk) match {
          case Failure(exception) =>
            val message = s"Failed to load domain pk=$domainPk. ${exception.getMessage}"
            logError(message)
            throw VitalsException(message)
          case Success(domain) => domain.copy(pk = datasetId)
        }
        val views = viewPks.map(viewPk => {
          catalog.findViewByPk(viewPk) match {
            case Failure(exception) =>
              val message = s"Failed to load view pk=$viewPk. ${exception.getMessage}"
              logError(message)
              throw VitalsException(message)
            case Success(view) => view.copy(pk = nextGeneratedId(), domainFk = datasetId)
          }
        })
        (domain, views)
      }

      for (view <- views) {
        val reloadInterval = loadIntervals(view.viewKey)
        val loadQuery = loadQueries.getOrElse(view.viewKey, batch.defaultLoadQuery.get)
        datasets += BurnInRunDataset(domain, view, loadQuery, batch.queries, reloadInterval)
      }
    }

    BurnInRunBatch(batch, System.currentTimeMillis(), datasets.toArray)
  }
}

case class BurnInRunBatch(
                           config: BurnInBatch,
                           startTimeMillis: Long,
                           datasets: Array[BurnInRunDataset],
                         )(implicit
                           agent: AgentService,
                           catalog: CatalogService,
                           registerEvent: BurnInEvent => Unit,
                           outerShouldContinue: () => Boolean,
                         ) {

  private var _didCreateDatasets = false

  private val _nextDatasetCounter = new AtomicInteger()

  private val _totalDatasetCounter = new AtomicInteger()

  private var workers = Array[BurnInWorker]()


  private def maxDurationExceeded: Boolean = config.maxDuration.exists(_.toMillis + startTimeMillis < System.currentTimeMillis())

  private def shouldContinue: Boolean = outerShouldContinue() && !maxDurationExceeded

  def ensureDatasets(): Unit = {
    if (!shouldContinue) {
      return
    }
    _didCreateDatasets = true
    log debug ("Creating datasets {}", datasets)
    registerLogEvent(s"Creating ${datasets.length} temporary datasets")
    val createdDomains = ConcurrentHashMap.newKeySet[Long](datasets.length)
    for (dataset <- datasets) {
      if (!createdDomains.contains(dataset.domain.domainKey)) {
        registerLogEvent(s"Inserting domain ${dataset.domain.domainKey}")
        catalog.insertDomainWithPk(CatalogDomain(dataset.domain, s"Burn-In Domain ${dataset.domain.domainKey}", udk = None, Some(Map(BurnInLabel -> "")))) match {
          case Failure(e) => registerLogEvent(s"Failed to insert domain pk=${dataset.domain.domainKey}: ${e.getMessage}", Level.INFO)
          case Success(_) => createdDomains.add(dataset.domain.domainKey)
        }
      }
      registerLogEvent(s"Inserting view ${dataset.view.viewKey}")
      catalog.insertViewWithPk(CatalogView(dataset.view, "moniker", udk = None, Some(Map(BurnInLabel -> "")))) match {
        case Failure(e) => registerLogEvent(s"Failed to insert view pk=${dataset.view.viewKey}: ${e.getMessage}", Level.INFO)
        case Success(_) => dataset.ready = true
      }
    }
  }

  def run(): BurnInRunBatchStats = {
    if (!shouldContinue) {
      return BurnInRunBatchStats(BatchDidNotRun)
    }
    workers = (0 until config.concurrency).map { i =>
      BurnInWorker(i, agent, catalog, this.workerShouldContinue, this.nextDataset, registerEvent)
    }.toArray
    val workerRuns = workers.map(_.run()).toSeq
    val mergedStats = Future.reduceLeft(workerRuns)(_.merge(_))
    registerLogEvent(s"Run started waiting ${config.runWait} for results. duration=${config.desiredDuration} max=${config.maxDuration}")
    Await.result(mergedStats, config.runWait)
  }

  private def workerShouldContinue(): Boolean = {
    if (!shouldContinue) {
      return false
    }

    if (config.durationSource == DurationType.ByIterations) {
      shouldContinueIterations
    } else if (config.durationSource == DurationType.ByDuration) {
      shouldContinueDuration
    } else {
      throw VitalsException("Unable to determine if burn-in workers should continue")
    }
  }

  private def shouldContinueIterations: Boolean = _totalDatasetCounter.get() <= config.desiredDatasetIterations.get

  private def shouldContinueDuration: Boolean = System.currentTimeMillis() < startTimeMillis + config.desiredDuration.get.toMillis

  private def nextDataset(): BurnInRunDataset = {
    val _ = _totalDatasetCounter.getAndIncrement()
    datasets(_nextDatasetCounter.getAndUpdate(n => (n + 1) % datasets.length))
  }

  def cleanUp(): Unit = {
    if (!_didCreateDatasets) {
      return
    }
    registerLogEvent(s"Cleaning up ${datasets.length} temporary datasets")
    for (dataset <- datasets) {
      catalog.deleteView(dataset.view.viewKey)
      catalog.deleteDomain(dataset.domain.domainKey)
    }
  }

  private def registerLogEvent(message: String, level: Level = Level.INFO): Unit = {
    registerEvent(BurnInLogEvent(message, level))
  }

}

case class BurnInWorker(
                         workerId: Int,
                         agent: AgentService,
                         catalog: CatalogService,
                         shouldContinue: () => Boolean,
                         nextDataset: () => BurnInRunDataset,
                         registerEvent: BurnInEvent => Unit
                       ) {

  def run(): Future[BurnInRunBatchStats] = {
    TeslaRequestFuture {
      var result = BurnInRunBatchStats(BatchCompletedNormally)
      while (shouldContinue()) breakable {
        BurnInDatasetTrek.begin() { stage =>
          val dataset = nextDataset()
          if (!dataset.ready) {
            break()
          }

          val guidBase = s"BurnIn_d${Math.abs(dataset.view.domainKey)}_v${Math.abs(dataset.view.viewKey)}"
          flushDataset(dataset)
          registerLogEvent(s"Loading dataset view=${dataset.view.viewKey}", Level.FINE)
          if (!shouldContinue()) {
            BurnInDatasetTrek.end(stage)
            break()
          }

          runQuery(dataset, dataset.loadQuery, s"${guidBase}_Load") match {

            case Failure(exception) =>
              stage.addEvent("Load failed")
              BurnInDatasetTrek.fail(stage, exception)
              registerLogEvent(s"Failed to load view=${dataset.view.viewKey}: ${exception.getMessage}", Level.WARNING)

            case Success(loadStats) =>
              registerLogEvent(s"Loaded dataset, beginning queries view=${dataset.view.viewKey}", Level.FINE)
              result = result.merge(loadStats)
              //            reportStats(loadStats)
              for (queryIdx <- dataset.queries.indices) {
                val query = dataset.queries(queryIdx)
                if (!shouldContinue()) {
                  BurnInDatasetTrek.end(stage)
                  break()
                }
                runQuery(dataset, query, s"${guidBase}_Query$queryIdx") match {
                  case Failure(exception) =>
                    registerLogEvent(s"Failed to execute query  $queryIdx on view ${dataset.view.viewKey}: ${exception.getMessage}", Level.WARNING)
                  case Success(queryStats) =>
                    result = result.merge(queryStats)
                  //                  reportStats(queryStats)
                }
              }
              BurnInDatasetTrek.end(stage)
          }
        }
      }
      result
    }
  }

  private def registerLogEvent(message: String, level: Level = Level.INFO): Unit = {
    registerEvent(BurnInLogEvent(message, level))
  }

  private def runQuery(dataset: BurnInRunDataset, query: String, guid: String): Try[BurnInRunBatchStats] = {
    if (dataset.shouldFlush) {
      Await.ready(flushDataset(dataset), 10.seconds)
    }
    dataset.willRunQuery()

    BurnInQueryTrek.begin(guid) { stage =>
      val over = FabricOver(dataset.domain.domainKey, dataset.view.viewKey)
      val future = agent.execute(query, over, guid)

      while (shouldContinue() && !future.isCompleted) {
        try {
          Await.ready(future, 5.seconds)
        } catch safely {
          case _: TimeoutException => // loop again if the future isn't ready yet
        }
      }

      future.value match {
        case Some(value) =>
          value.flatMap(result => result.resultStatus match {
            case status.FabricSuccessResultStatus
                 | status.FabricNoDataResultStatus =>
              BurnInQueryTrek.end(stage)
              Success(BurnInRunBatchStats(result))

            case status.FabricInProgressResultStatus
                 | status.FabricUnknownResultStatus
                 | status.FabricFaultResultStatus
                 | status.FabricInvalidResultStatus
                 | status.FabricTimeoutResultStatus
                 | status.FabricNotReadyResultStatus
                 | status.FabricStoreErrorResultStatus =>
              val exception = VitalsException(s"Query execution failed: ${result.resultMessage}")
              BurnInQueryTrek.fail(stage, exception)
              Failure(exception)

            case _ =>
              val exception = VitalsException(s"Query execution unknown status: ${result.resultMessage}")
              BurnInQueryTrek.fail(stage, exception)
              Failure(exception)
          })
        case None =>
          val exception = VitalsException("Query execution incomplete")
          BurnInQueryTrek.fail(stage, exception)
          Failure(exception)
      }
    }
  }

  private def flushDataset(dataset: BurnInRunDataset) = {
    dataset.flushed()
    agent.cacheGenerationOp(uid.newBurstUid, FabricCacheFlush, dataset.generationKey, parameters = None) // flush
  }
}
