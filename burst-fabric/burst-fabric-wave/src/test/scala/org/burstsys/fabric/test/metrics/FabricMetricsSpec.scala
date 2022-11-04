/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.metrics

import java.util.concurrent.TimeUnit

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.{FabricParticle, FabricWave}
import org.burstsys.fabric.wave.metadata.model
import org.burstsys.fabric.wave.metadata.model._
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.fabric.test.mock
import org.burstsys.fabric.test.mock.MockScanner
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class FabricMetricsSpec extends FabricMetricsBaseSpec with FabricTopologyListener {


  it should "do a wave execution and collect metrics" in {

    val guid = newBurstUid
    val promise = Promise[FabricGather]()


    // first make sure the worker is connected
    newWorkerGate.await(10, TimeUnit.SECONDS) should equal(true)

    val quo: BrioSchema = BrioSchema("quo")

    // get an appropriate datasource
    val datasource: FabricDatasource = model.datasource.FabricDatasource(
      FabricDomain(domainKey = domainKey),
      FabricView(
        domainKey = domainKey, viewKey = viewKey, generationClock = generationClock,
        schemaName = quo.name, storeProperties = Map(FabricStoreNameProperty -> mock.MockStoreName),
        viewProperties = Map.empty
      )
    )

    // handy dandy mock scanner
    val scanner = MockScanner(datasource.view.schemaName).initialize(
      FabricGroupKey(groupName = "mockgroup", groupUid = guid),
      datasource
    )

    def FAIL(t: Throwable): Unit = {
      log error s"FAIL $t"
      promise.failure(t)
    }

    supervisorContainer.data.slices(guid, datasource) onComplete {
      case Failure(t) => FAIL(t)
      case Success(slices) =>
        Try {
          // get appropriate set of slices and create particles out of them
          val particles = slices map (slice => FabricParticle(guid, slice, scanner))
          // create a wave from the particles
          FabricWave(guid, particles)
        } match {
          case Failure(t) => FAIL(t)
          case Success(wave) =>
            supervisorContainer.execution.executionWaveOp(wave) onComplete {
              case Failure(t) => FAIL(t)
              case Success(gather) => promise.success(gather)
            }
        }
    }

    val itemCount = 500
    val sliceCount = 2
    val regionCount = Runtime.getRuntime.availableProcessors * sliceCount

    // execute the wave - wait for future - get back a gather
    val gather = Await.result(promise.future, 10 minutes)

    val metrics = gather.gatherMetrics
    log info metrics.toString

    metrics.generationKey.domainKey should equal(domainKey)
    metrics.generationKey.viewKey should equal(viewKey)
    metrics.generationKey.generationClock should equal(generationClock)
    metrics.generationMetrics.generationKey.domainKey should equal(domainKey)
    metrics.generationMetrics.generationKey.viewKey should equal(viewKey)
    metrics.generationMetrics.generationKey.generationClock should equal(generationClock)

    metrics.generationMetrics.byteCount should equal(2882152)
    metrics.generationMetrics.itemCount should equal(itemCount * sliceCount)
    metrics.generationMetrics.regionCount should equal(regionCount)
    metrics.generationMetrics.sliceCount should equal(sliceCount)

/*
    metrics.executionMetrics.scanTime.toInt should be(70876499 +- (70876499 / 2))
    metrics.executionMetrics.scanWork.toInt should be(90571463 +- (90571463 / 2))
*/

  }

}
