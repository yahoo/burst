/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada

import org.apache.logging.log4j.Level
import org.burstsys.brio.types.BrioTypes
import org.burstsys.catalog.model.view._
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops.FabricCacheFlush
import org.burstsys.fabric.wave.execution.model.result._
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.wave.execution.model.result.status._
import org.burstsys.fabric.wave.metadata.model.over
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 *
 */
final
case class Executor(driver: Driver) extends Callable[Unit] with VitalsLogger {

  override def call(): Unit = {
    while (stillLoading) {
      val index = driver.jobSettings.counter.getAndIncrement() % driver.jobSettings.datasets.length
      val dataset = driver.jobSettings.datasets(index)
      val domainKey = dataset.domainId
      val viewKey = dataset.viewId

      // first query loads
      report('l')
      val firstTime = driver.jobSettings.statTable.startQueryTimer(dataset, first = true)
      val loadStart = System.nanoTime()
      val query = defaultQuery(driver.jobSettings.schemaName)
      val guid: String = s"TORCHER_LOAD_${domainKey}_${viewKey}_$loadStart"

      val promise = Promise[Unit]()
      driver.agentClient.execute(query, over.FabricOver(domainKey, viewKey), guid, call = None) onComplete {
        case Success(r) =>
          if (!r.succeeded) {
            report('F')
            r.resultStatus match {
              case FabricNoDataResultStatus =>
                driver.jobSettings.notifyListeners(Level.INFO, burstStdMsg(s"NO-DATA found for first query, view: $viewKey"))
                driver.jobSettings.statTable.recordQueryNoDataFailure(dataset, guid, first = true)
              case _ =>
                driver.jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"failure running first query: ${r.resultMessage}"))
                driver.jobSettings.statTable.recordQueryFailure(dataset, guid, new RuntimeException(s"invalid query api status: ${r.resultMessage}"), first = true)
                if (driver.jobSettings.stopOnFail) {
                  driver.stop
                }
            }
          } else {
            firstTime.stop()
            driver.jobSettings.statTable.recordStatistics( dataset, guid, r.resultGroup.get.groupMetrics)
            report(checkResult(dataset, r))
            runQueries(loadStart, dataset)
          }
          promise.success((): Unit)
        case Failure(e) =>
          report('F')
          driver.jobSettings.statTable.recordQueryFailure(dataset, guid, e, first = true)
          val msg = burstStdMsg(s"failure running first query $query", e)
          driver.jobSettings.notifyListeners(Level.ERROR, msg)
          if (driver.jobSettings.stopOnFail) {
            driver.stop
          }
          promise.failure(e)
      }

      // wait for the COLD_QUERY/HOT_QUERIES BATCH to complete
      Await.result(promise.future, 10 minutes)

      // do optional FLUSH
      if (dataset.flush == 'F') {
        report('f')
        Await.ready(driver.agentClient.cacheGenerationOp(newBurstUid, FabricCacheFlush, FabricGenerationKey(domainKey, viewKey), None), 10 minutes)
        report('.')
      } else if (dataset.flush == 'G') {
        report('g')
        driver.catalogClient.findViewByPk(dataset.viewId) match {
          case Success(view) =>
            val newView = view.copy(generationClock = System.currentTimeMillis())
            driver.catalogClient.ensureView(newView) match {
              case Success(_) =>
                driver.jobSettings.notifyListeners(Level.DEBUG, burstStdMsg(s"updated view ${dataset.viewId}"))
                report('.')
              case Failure(e) =>
                driver.jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"failure updating gen key for view ${dataset.viewId}", e))
                if (driver.jobSettings.stopOnFail) {
                  driver.stop
                }
            }
          case Failure(e) =>
            driver.jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"failure retrieving view ${dataset.viewId} for gen update", e))
            if (driver.jobSettings.stopOnFail) {
              driver.stop
            }
        }
      }

      // see if we have to sleep to make the LPS work
      val delay = if (driver.jobSettings.loadDelay.toNanos > 0)
        loadStart + driver.jobSettings.loadDelay.toNanos - System.nanoTime()
      else 0
      if (delay > 0) {
        report('s')
        Thread.sleep(delay / 1000000, (delay % 1000000).toInt)
        report('.')
      }
    }
    reportFlush()
  }

  private
  def checkResult(dataset: Dataset, es: FabricExecuteResult): String = {
    if (es.resultStatus != FabricSuccessResultStatus)
      return s"!-${}rg.context.message}"
    else if (es.resultGroup.get.resultSets.isEmpty)
      return s"X"

    val resultGroup = es.resultGroup.get
    val resultSet: FabricResultSet = resultGroup.resultSets(0)
    val resultSetMetrics = resultSet.metrics

    // validate the overflows return nothing
    if (resultSetMetrics.overflowed && resultSet.rowCount > 0) {
      val msg = burstStdMsg(s"A query on domainKey=${dataset.domainId} viewKey=${dataset.viewId} " +
        s"overflowed but returned ${resultSet.rowCount} rows")
      driver.jobSettings.notifyListeners(Level.ERROR, msg)
    }

    if (resultSet.rowSet.isEmpty)
      return s"0"
    val cIndex = resultSet.columnNames.indexOf("count")
    if (cIndex < 0)
      return "."

    val cell = resultSet.rowSet.head(cIndex)
    val c = cell.bType match {
      case BrioTypes.BrioByteKey => cell.asByte.toLong
      case BrioTypes.BrioShortKey => cell.asShort.toLong
      case BrioTypes.BrioIntegerKey => cell.asInteger.toLong
      case BrioTypes.BrioLongKey => cell.asLong
      case BrioTypes.BrioDoubleKey => cell.asDouble.toLong
      case _ => 0.toLong
    }
    driver.jobSettings.statTable.recordObjectCount(dataset, c)

    if (c == 0) "." else s"($c)"
  }

  private
  def runQueries(loadStart: Long, dataset: Dataset): Unit = {
    // run some number of queries until the load delay is passed
    val queryIndex = new AtomicInteger(0)
    while (stillQuerying(dataset.queries, queryIndex.getAndIncrement, loadStart)) {
      val promise = Promise[Unit]()
      val query = dataset.queries(queryIndex.get % dataset.queries.length)
      report(s"q")
      val guid: String = s"TORCHER_Q${queryIndex.get}_${dataset.domainId}_${dataset.viewId}_$loadStart"
      val queryTime = driver.jobSettings.statTable.startQueryTimer(dataset)
      driver.agentClient.execute(query, over.FabricOver(dataset.domainId, dataset.viewId), guid, call = None) onComplete {
        case Success(r) =>
          if (!r.succeeded) {
            report(s"f")
            driver.jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"failure running query $query: ${r.resultMessage}"))
            driver.jobSettings.statTable.recordQueryFailure(dataset, guid, new RuntimeException(s"invalid query api status: ${r.resultMessage}"))
          } else {
            // see if we have to sleep to make the QPS work
            val elapsed = queryTime.stop()
            report(s".")
            val wait = if (driver.jobSettings.queryDelay.toNanos > 0)
              driver.jobSettings.queryDelay.toNanos - elapsed
            else
              0L
            if (wait > 0) {
              report(s"s")
              Thread.sleep(wait / 1000000, (wait % 1000000).toInt)
              report('.')
            }
          }
          promise.success(())
        case Failure(e) => report('!')
          report(s"f")
          driver.jobSettings.notifyListeners(Level.ERROR, burstStdMsg(s"failure running query $query", e))
          driver.jobSettings.statTable.recordQueryFailure(dataset, guid, e)
          promise.failure(e)
      }
      // wait for the QUERY request to complete
      Await.result(promise.future, 10 minutes)
    }
  }

  /**
   * Determine if the query loop should keep going
   */
  private
  def stillQuerying(queries: List[String], queryIndex: Int, loadStart: Long): Boolean = {
    if (!driver.torcherRunning.get())
    // stop if we were flagged as done
    false
    else if (driver.jobSettings.queryDelay.toNanos > 0 && driver.jobSettings.loadDelay.toNanos > 0)
    // there is a LPS target and a QPS target so we run queries until the LPS target is achieved
    System.nanoTime() < loadStart + driver.jobSettings.loadDelay.toNanos
    else
    // in all other cases (QPS or LPS or neither specified) just run through the query list once
    queryIndex < queries.length
  }

  def stillLoading: Boolean = {
    driver.torcherRunning.get() && (
      if (driver.jobSettings.duration.toNanos > 0)
        System.nanoTime() < driver.jobSettings.startNanos.get() + driver.jobSettings.duration.toNanos
      else
        driver.jobSettings.counter.get() < driver.jobSettings.datasets.length
      )
  }

  private var txt: String = ""

  private def report(mark: Char): Unit = report(s"$mark")

  private def report(mark: String): Unit = {
    txt += s"$mark"
    if (txt.length > 61)
      reportFlush()
  }

  private def reportFlush(): Unit = {
    if (driver.torcherParameters.verbose && !txt.isEmpty)
      driver.jobSettings.notifyListeners(Level.INFO, burstStdMsg(txt))
    txt = ""
  }
}
