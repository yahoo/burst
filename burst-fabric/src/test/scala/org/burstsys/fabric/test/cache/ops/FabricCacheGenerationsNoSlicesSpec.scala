/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.cache.ops

import java.util.concurrent.TimeUnit

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.FabricCacheSearch
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.wave.{FabricParticle, FabricWave}
import org.burstsys.fabric.metadata.model
import org.burstsys.fabric.metadata.model._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.test.mock
import org.burstsys.fabric.test.mock.MockScanner
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class FabricCacheGenerationsNoSlicesSpec extends FabricCacheOpsBaseSpec with FabricTopologyListener {


  it should "fetch all generations in lite JSON form" in {

    val guid1 = newBurstUid
    val promise1 = Promise[FabricGather]

    // first make sure the worker is connected
    newWorkerGate.await(10, TimeUnit.SECONDS) should equal(true)

    val quo: BrioSchema = BrioSchema("quo")

    // get an appropriate datasource
    val datasource: FabricDatasource = FabricDatasource(
      FabricDomain(domainKey = domainKey),
      FabricView(
        domainKey = domainKey, viewKey = viewKey, generationClock = generationClock,
        schemaName = quo.name, storeProperties = Map(FabricStoreNameProperty -> mock.MockStoreName),
        viewProperties = Map.empty
      )
    )

    // handy dandy mock scanner
    val scanner = MockScanner(datasource.view.schemaName).initialize(
      FabricGroupKey(groupName = "mockgroup", groupUid = guid1),
      datasource
    )

    def FAIL(t: Throwable): Unit = {
      log error s"FAIL $t"
      promise1.failure(t)
    }

    masterContainer.data.slices(guid1, datasource) onComplete {
      case Failure(t) => FAIL(t)
      case Success(slices) =>
        Try {
          // get appropriate set of slices and create particles out of them
          val particles = slices map (slice => FabricParticle(guid1, slice, scanner = scanner))
          // create a wave from the particles
          FabricWave(guid1, particles)
        } match {
          case Failure(t) => FAIL(t)
          case Success(wave) =>
            masterContainer.execution.executionWaveOp(wave) onComplete {
              case Failure(t) => FAIL(t)
              case Success(gather) => promise1.success(gather)
            }
        }
    }
    // execute the wave - wait for future - get back a gather
    Await.result(promise1.future, 10 minutes)

    val guid = newBurstUid
    val promise = Promise[Array[FabricGeneration]]
    masterContainer.data.cacheGenerationOp(guid, FabricCacheSearch,  FabricGenerationKey(), None) onComplete {
      case Failure(t) => promise.failure(t)
      case Success(r) =>
        val generations = r.map(_.toJsonLite).toArray
        promise.success(generations)
    }

    val result = Await.result(promise.future, 10 minutes)

    result.length should be(1)
    result.head.slices.length should be (0)


  }

}
