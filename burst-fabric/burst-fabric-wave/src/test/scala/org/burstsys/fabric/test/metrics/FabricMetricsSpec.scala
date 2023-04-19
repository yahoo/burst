/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.metrics

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.test.mock
import org.burstsys.fabric.test.mock.MockScanner
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.FabricParticle
import org.burstsys.fabric.wave.execution.model.wave.FabricWave
import org.burstsys.fabric.wave.metadata.model
import org.burstsys.fabric.wave.metadata.model._
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.uid._

import java.util.Date
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class FabricMetricsSpec extends FabricWaveSupervisorWorkerBaseSpec {

  val domainKey: FabricDomainKey = 1
  val viewKey: FabricViewKey = 1
  val generationClock: FabricGenerationClock = new Date().getTime

  override def wantsContainers = true

  override def workerCount = 2

  it should "do a wave execution and collect metrics" in {
    val itemCount = 500
    val sliceCount = 2
    val regionCount = Runtime.getRuntime.availableProcessors * sliceCount

    val guid = newBurstUid
    val promise = Promise[FabricGather]()

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

    log info s"healthy workers count=${supervisorContainer.topology.healthyWorkers.length}"
    supervisorContainer.topology.healthyWorkers.length shouldEqual sliceCount

    val result = supervisorContainer.data.slices(guid, datasource) flatMap { slices =>
      log info s"sliced mock data sliceCount=${slices.length}"
      // get appropriate set of slices and create particles out of them
      val particles = slices.map(FabricParticle(guid, _, scanner))
      // create a wave from the particles
      val wave = FabricWave(guid, particles)
      supervisorContainer.execution.executionWaveOp(wave)
    }
    val gather = Await.result(result, 2 minutes)

    val metrics = gather.gatherMetrics
    log info metrics.toString

    metrics.generationKey.domainKey shouldEqual domainKey
    metrics.generationKey.viewKey shouldEqual viewKey
    metrics.generationKey.generationClock shouldEqual generationClock
    metrics.generationMetrics.generationKey.domainKey shouldEqual domainKey
    metrics.generationMetrics.generationKey.viewKey shouldEqual viewKey
    metrics.generationMetrics.generationKey.generationClock shouldEqual generationClock

    metrics.generationMetrics.byteCount shouldEqual 2882152
    metrics.generationMetrics.itemCount shouldEqual itemCount * sliceCount
    metrics.generationMetrics.regionCount shouldEqual regionCount
    metrics.generationMetrics.sliceCount shouldEqual sliceCount
  }

}
