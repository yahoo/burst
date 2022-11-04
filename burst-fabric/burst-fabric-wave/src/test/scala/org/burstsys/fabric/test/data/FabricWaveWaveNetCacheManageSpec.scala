/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.data

import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorListener
import org.burstsys.fabric.wave.container.worker.FabricWaveWorkerListener
import org.burstsys.fabric.wave.data
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops.FabricCacheSearch
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.vitals.uid._

import java.util.concurrent.{CountDownLatch, TimeUnit}

class FabricWaveWaveNetCacheManageSpec extends FabricWaveSupervisorWorkerBaseSpec
  with FabricWaveSupervisorListener with FabricWaveWorkerListener with FabricTopologyListener {

  override protected def wantsContainers = true

  override protected
  def beforeAll(): Unit = {
    data.worker.cache.instance.start
    supervisorContainer.talksTo(this)
    supervisorContainer.topology.talksTo(this)
    workerContainer1.talksTo(this)
    super.beforeAll()
  }

  override protected
  def afterAll(): Unit = {
    super.afterAll()
    data.worker.cache.instance.stop
  }

  val newWorkerGate = new CountDownLatch(1)
  val sliceFetchGate = new CountDownLatch(2)
  val operationGate = new CountDownLatch(2)

  it should "receive and reply to all messages" in {

    val guid = newBurstUid

    newWorkerGate.await(30, TimeUnit.SECONDS) should equal(true)

    val key = FabricGenerationKey()

    log info s"----------------------- cacheOperation"
    supervisorContainer.data.cacheGenerationOp(guid, FabricCacheSearch, key, None)
    operationGate.await(30, TimeUnit.SECONDS) should equal(true)

    log info s"----------------------- sliceFetch"
    supervisorContainer.data.cacheSliceOp(guid, key)
    sliceFetchGate.await(30, TimeUnit.SECONDS) should equal(true)
  }

  override
  def onNetClientCacheOperationReqMsg(connection: FabricNetClientConnection, msg: FabricNetCacheOperationReqMsg): Unit = operationGate.countDown()

  override
  def onNetServerCacheOperationRespMsg(connection: FabricNetServerConnection, msg: FabricNetCacheOperationRespMsg): Unit = {
    operationGate.countDown()
  }

  override
  def onNetServerSliceFetchRespMsg(connection: FabricNetServerConnection, msg: FabricNetSliceFetchRespMsg): Unit = {
    sliceFetchGate.countDown()
  }

  override
  def onNetClientSliceFetchReqMsg(connection: FabricNetClientConnection, msg: FabricNetSliceFetchReqMsg): Unit = sliceFetchGate.countDown()

  override
  def onTopologyWorkerGained(worker: FabricTopologyWorker): Unit = newWorkerGate.countDown()

}
