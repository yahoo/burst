/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.support

import org.burstsys.alloy.alloy.usecase.AlloyJsonUseCaseRunner
import org.burstsys.eql.EqlContext
import org.burstsys.eql.context.EqlContextImpl
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.fabric.wave.metadata.model.FabricViewKey
import org.burstsys.hydra.HydraService
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors._
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.uid._

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

abstract class EqlAlloyTestRunner extends AlloyJsonUseCaseRunner with EqlTestLog {

  override def localStartup(): Unit = {
    log info s"Starting Eql Alloy Test Runner"
    eql = new EqlContextImpl(newBurstUid)
    hydra = HydraService(supervisorContainer).start
  }

  override def localShutdown(): Unit = {
    log info s"Stopping Eql Alloy Test Runner"
    hydra.stop
  }

  override def localAfterStartup(): Unit = super.localAfterStartup()

  var hydra: HydraService = _
  var eql: EqlContext = _

  def runTest(eqlSource: String, domainKey: FabricDomainKey, viewKey: FabricViewKey,
              validate: FabricResultGroup => Unit,
              parameters: String = "{}"
             ): Long =
  {
    var elapsedTime: Long = 0
    val promise = Promise[Unit]()
    TeslaRequestCoupler {
      Try {
        eql.eqlToHydra(None, eqlSource)
      } match {
        case Failure(t) =>
          promise.failure(t)
        case Success(r) =>
          val source = r
          log info s"Hydra source:\n $source"
          val startTime = System.nanoTime()
          hydra.executeHydraAsWave(newBurstUid, source, FabricOver(domain = domainKey, view = viewKey), Some(FabricCall(parameters))) onComplete {
            case Failure(t) =>
              promise.failure(t)
            case Success(result) =>
              elapsedTime = System.nanoTime() - startTime
              Try {
                if (result.resultStatus.isFailure) {
                  val msg = s"execution of hydra source '$source' ${result.resultStatus}"
                  throw VitalsException(msg)
                }
                validate(result)
              } match {
                case Failure(t) =>
                  log error s"parsing of '$eqlSource' FAIL[${messageFromException(t)}]"
                  log error s"hydra generated:\n$source"
                  promise.failure(t)
                case Success(r) =>
                  promise.success((): Unit)
              }
          }
      }
    }
    Await.result(promise.future, 10 minutes)
    elapsedTime
  }

  def runTest(eqlSource: String, domainKey: FabricDomainKey, viewKey: FabricViewKey,
              validate: FabricResultGroup => Unit,
              parameters: String,
              parallelism: Int,
              iterations: Int
             ): Long =
  {
    val parTests = (0 until parallelism).map{_ => Promise[Double]()}
    val parElapsedTime: AtomicLong = new AtomicLong(0)
    for (i <- 0 until parallelism) {
      val parTest = parTests(i)
      Future {
        log info s"starting test thread $i"
        val totalElapsedTime: AtomicLong = new AtomicLong(0)
        Try {
          eql.eqlToHydra(None, eqlSource)
        } match {
          case Failure(t) =>
            val msg = s"($i) failed to parse EQL source '$eqlSource'"
            parTest.failure(VitalsException(msg, t))
          case Success(r) =>
            val source = r
            log debug s"Hydra source:\n $source"
            for (j <- 0 to iterations) {
              val promise = Promise[Long]()

              val startTime = System.nanoTime()
              hydra.executeHydraAsWave(newBurstUid, source, FabricOver(domain = domainKey, view = viewKey), Some(FabricCall(parameters))) onComplete {
                case Failure(t) =>
                  promise.failure(t)
                case Success(result) =>
                  val elapsedTime = System.nanoTime() - startTime
                  Try {
                    if (result.resultStatus.isFailure) {
                      val msg = s"($i, $j) execution of hydra source '$source' ${result.resultStatus}"
                      throw VitalsException(msg)
                    }
                    validate(result)
                  } match {
                    case Failure(t) =>
                      log error s"parsing of '$eqlSource' FAIL[${messageFromException(t)}]"
                      log error s"hydra generated:\n$source"
                      promise.failure(t)
                    case Success(r) =>
                      log debug s"($i, $j) execution in ${prettyTimeFromNanos(elapsedTime)} ($elapsedTime ns)"
                      totalElapsedTime.addAndGet(elapsedTime)
                      promise.success(elapsedTime)
                  }
              }
              Await.ready(promise.future, 100 minutes).value.get match {
                case Success(_) =>
                case Failure(e) =>
                  val re = if (e.getCause != null) e.getCause else e
                  parTest.failure(re)
                  promise.failure(re)
                  throw re
              }
            }
        }
        log info s"end test thread $i"
        parElapsedTime.addAndGet(totalElapsedTime.get())
        parTest.success(totalElapsedTime.get().toDouble)
      }
    }
    Await.ready(Future.sequence(parTests.map(_.future)), 100 minutes).value.get match {
      case Success(t) =>
        parElapsedTime.get/(parallelism * iterations)
      case Failure(e) =>
        throw if (e.getCause != null) e.getCause else e
    }
  }

  def checkResults(result: FabricResultGroup): FabricResultSet = {
    if (!result.resultStatus.isSuccess)
      throw VitalsException(s"execution failed: ${result.resultStatus}")
    if (result.groupMetrics.executionMetrics.overflowed > 0)
      throw VitalsException(s"execution overflowed")
    if (result.groupMetrics.executionMetrics.limited > 0)
      throw VitalsException(s"execution limited")

    // all the besides should return a result set
    result.resultSets.keys.size should be > 0

    result.resultSets(0)
  }


  def toHydraSource(eqlSource: String): String = {
    eql.eqlToHydra(None, eqlSource)
  }

}
