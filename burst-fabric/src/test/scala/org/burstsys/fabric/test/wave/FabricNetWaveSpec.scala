/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.wave

import java.util.Date
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.execution.master.wave.{ParticleDispatched, ParticleFailed, ParticleSucceeded, WaveBegan, WaveFailed, WaveSucceeded}
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.pipeline.{FabricPipelineEvent, FabricPipelineEventListener, addPipelineSubscriber}
import org.burstsys.fabric.execution.model.wave.{FabricParticle, FabricWave}
import org.burstsys.fabric.metadata.model._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.net.client.FabricNetClientListener
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.burstsys.fabric.test.mock.{MockScanner, MockStoreMaster, MockStoreName, MockStoreWorker}
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class FabricNetWaveSpec extends FabricMasterWorkerBaseSpec
  with FabricNetServerListener with FabricNetClientListener with FabricTopologyListener with FabricPipelineEventListener {

  val mockStoreMaster: MockStoreMaster = MockStoreMaster(masterContainer)
  val mockStoreWorker: MockStoreWorker = MockStoreWorker(workerContainer1)

  override def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    masterContainer.netServer.talksTo(this)
    masterContainer.topology.talksTo(this)
    workerContainer1.netClient.talksTo(this)
    addPipelineSubscriber(this)
  }

  override protected
  def afterAll(): Unit = {
    super.afterAll()
  }

  val newWorkerGate = new CountDownLatch(1)
  val waveBeginGate = new CountDownLatch(1)
  val particleBeginGate = new CountDownLatch(1)
  val particleSucceedGate = new CountDownLatch(1)
  val waveSucceedGate = new CountDownLatch(1)

  val domainKey: FabricDomainKey = 1
  val viewKey: FabricViewKey = 1
  val generationClock: FabricGenerationClock = new Date().getTime

  it should "do a wave execution" in {

    val guid = newBurstUid

    // first make sure the worker is connected
    newWorkerGate.await(10, TimeUnit.SECONDS) should equal(true)

    val quo = BrioSchema("quo")
    // get an appropriate datasource
    val datasource = FabricDatasource(
      FabricDomain(domainKey = domainKey),
      FabricView(
        domainKey = domainKey, viewKey = viewKey, generationClock = generationClock,
        schemaName = quo.name, storeProperties = Map(FabricStoreNameProperty -> MockStoreName),
        viewProperties = Map.empty
      )
    )

    // handy dandy mock scanner
    val scanner = MockScanner(datasource.view.schemaName).initialize(
      FabricGroupKey(groupName = "mockgroup", groupUid = guid),
      datasource
    )

    val promise = masterContainer.data.slices(guid, datasource) flatMap { slices =>
      // get appropriate set of slices and create particles out of them
      val particles = slices map (FabricParticle(guid, _, scanner))
      // create a wave from the particles
      val wave = FabricWave(guid, particles)
      masterContainer.execution.executionWaveOp(wave)
    }

    // execute the wave - wait for future - get back a gather
    Await.result(promise, 10 minutes)

    assert(waveBeginGate.await(20, TimeUnit.SECONDS))
    assert(particleBeginGate.await(20, TimeUnit.SECONDS))
    assert(particleSucceedGate.await(20, TimeUnit.SECONDS))
    assert(waveSucceedGate.await(20, TimeUnit.SECONDS))

  }

  override def onEvent: PartialFunction[FabricPipelineEvent, Boolean] = {
    case e: WaveBegan =>
      log info s"################## onWaveBegin(seqNum=${e.seqNum}, guid=${e.guid})"
      waveBeginGate.countDown()
      true

    case e: ParticleDispatched =>
      log info s"################## onParticleBegin(seqNum=${e.seqNum}, guid=${e.guid}, ruid=${e.ruid})"
      particleBeginGate.countDown()
      true

    case e: ParticleSucceeded =>
      log info s"################## onParticleSucceed(seqNum=${e.seqNum}, guid=${e.guid}, ruid=${e.ruid})"
      particleSucceedGate.countDown()
      true

    case e: ParticleFailed =>
      log info s"################## onParticleFail(seqNum=${e.seqNum}, guid=${e.guid}, ruid=${e.ruid}, msg=${e.message})"
      true

    case e: WaveFailed =>
      log info s"################## onWaveFail(seqNum=${e.seqNum}, guid=${e.guid}, msg=${e.message})"
      true

    case e: WaveSucceeded =>
      log info s"################## onWaveSucceed(seqNum=${e.seqNum}, guid=${e.guid})"
      waveSucceedGate.countDown()
      true
  }

  override
  def onTopologyWorkerGained(worker: FabricWorkerNode): Unit = {
    newWorkerGate.countDown()
  }

}
