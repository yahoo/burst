/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.container.supervisor

import org.burstsys.fabric.wave.data.model.generation.FabricGeneration
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.ops.FabricCacheManageOp
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.wave.FabricParticle
import org.burstsys.fabric.net.server.connection.FabricNetServerConnection
import org.burstsys.tesla.scatter.slot.TeslaScatterSlot
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.Future

/**
 * handler for supervisor container events
 */
trait FabricWaveSupervisorAPI extends AnyRef {
  /**
   * execute one particle of a wave. Make sure this call is async i.e. do not take a long time
   * or wait around for IO operations to succeed. The Future idiom should take care of this
   */
  def executeParticle(connection: FabricNetServerConnection,
                      slot: TeslaScatterSlot,
                      particle: FabricParticle): Future[FabricGather]

  /**
   * manage caches on the workers
   */
  def cacheManageOperation(connection: FabricNetServerConnection,
                           guid: VitalsUid,
                           ruid: VitalsUid,
                           operation: FabricCacheManageOp,
                           generationKey: FabricGenerationKey): Future[Array[FabricGeneration]]

  /**
   * fetch slice metadata from the caches of the workers
   */
  def cacheSliceFetchOperation(connection: FabricNetServerConnection,
                               guid: VitalsUid,
                               ruid: VitalsUid,
                               generationKey: FabricGenerationKey): Future[Array[FabricSliceMetadata]]
}
