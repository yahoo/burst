/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.data

import org.burstsys.fabric.net.client.connection.FabricNetClientConnection
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.fabric.test.FabricWaveSupervisorWorkerBaseSpec
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.fabric.wave.container.supervisor.{FabricWaveSupervisorListener, MockWaveSupervisorContainer}
import org.burstsys.fabric.wave.container.worker.{FabricWaveWorkerListener, MockWaveWorkerContainer}
import org.burstsys.fabric.wave.data
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops.FabricCacheSearch
import org.burstsys.fabric.wave.message.cache.{FabricNetCacheOperationReqMsg, FabricNetCacheOperationRespMsg, FabricNetSliceFetchReqMsg, FabricNetSliceFetchRespMsg}
import org.burstsys.vitals.uid._

import java.util.concurrent.{CountDownLatch, TimeUnit}

class FabricWaveWaveNetCacheManageSpec extends FabricWaveSupervisorWorkerBaseSpec
  with FabricWaveSupervisorListener with FabricWaveWorkerListener {

  override protected def wantsContainers = true

  override protected def configureSupervisor(supervisor: MockWaveSupervisorContainer): Unit = {
    super.configureSupervisor(supervisor)
    supervisor.talksTo(this)
  }

  override protected def configureWorker(worker: MockWaveWorkerContainer): Unit = {
    super.configureWorker(worker)
    workerContainer1.talksTo(this)
  }

  val sliceFetchGate = new CountDownLatch(2)
  val operationGate = new CountDownLatch(2)

  it should "receive and reply to all messages" in {

    val guid = newBurstUid

    val key = FabricGenerationKey()

    log info s"$marker cacheOperation"
    supervisorContainer.data.cacheGenerationOp(guid, FabricCacheSearch, key, None)
    operationGate.await(30, TimeUnit.SECONDS) should equal(true)

    log info s"$marker sliceFetch"
    supervisorContainer.data.cacheSliceOp(guid, key)
    sliceFetchGate.await(30, TimeUnit.SECONDS) should equal(true)
  }

  override def onNetClientCacheOperationReqMsg(connection: FabricNetClientConnection, msg: FabricNetCacheOperationReqMsg): Unit = {
    operationGate.countDown()
  }

  override def onNetServerCacheOperationRespMsg(connection: FabricNetServerConnection, msg: FabricNetCacheOperationRespMsg): Unit = {
    operationGate.countDown()
  }

  override def onNetServerSliceFetchRespMsg(connection: FabricNetServerConnection, msg: FabricNetSliceFetchRespMsg): Unit = {
    sliceFetchGate.countDown()
  }

  override def onNetClientSliceFetchReqMsg(connection: FabricNetClientConnection, msg: FabricNetSliceFetchReqMsg): Unit = {
    sliceFetchGate.countDown()
  }

}
