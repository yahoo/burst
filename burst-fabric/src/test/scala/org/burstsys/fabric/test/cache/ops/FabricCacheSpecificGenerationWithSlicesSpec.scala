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
import org.burstsys.fabric.metadata.model._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.test.mock
import org.burstsys.fabric.test.mock.MockScanner
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class FabricCacheSpecificGenerationWithSlicesSpec extends FabricCacheOpsBaseSpec with FabricTopologyListener {


  it should "fetch all generations that match a spec with slices" in {

    val queryId = newBurstUid
    val promise1 = Promise[FabricGather]()

    // first make sure the worker is connected
    newWorkerGate.await(30, TimeUnit.SECONDS) should equal(true)

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
      FabricGroupKey(groupName = "mockgroup", groupUid = queryId),
      datasource
    )

    def FAIL(t: Throwable): Unit = {
      log error s"FAIL $t"
      promise1.failure(t)
    }

    masterContainer.data.slices(queryId, datasource) onComplete {
      case Failure(t) => FAIL(t)
      case Success(slices) =>
        Try {
          // get appropriate set of slices and create particles out of them
          val particles = slices map (slice => FabricParticle(queryId, slice, scanner))
          // create a wave from the particles
          FabricWave(queryId, particles)
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

    val searchId = newBurstUid
    val promise = Promise[FabricGeneration]()

    // specific generation
    val generationKey = FabricGenerationKey(
      domainKey = datasource.view.domainKey,
      viewKey = datasource.view.viewKey,
      generationClock = datasource.view.generationClock
    )
    masterContainer.data.cacheGenerationOp(searchId, FabricCacheSearch, generationKey, None) onComplete {
      case Failure(t) =>
        promise.failure(t)
      case Success(g) =>
        if (g.nonEmpty) {
          promise.success(g.head.toJson)
        } else {
          promise.failure(VitalsException(s"ERROR").fillInStackTrace())
        }
    }

    val result = Await.result(promise.future, 10 minutes)

    result.slices.length should be (2)
    result

  }

}
