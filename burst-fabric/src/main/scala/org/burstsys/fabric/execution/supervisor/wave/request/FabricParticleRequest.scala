/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.supervisor.wave.request

import org.burstsys.fabric.execution.supervisor.FabricScatteredGatherRequest
import org.burstsys.fabric.execution.model.wave.FabricParticle
import org.burstsys.tesla.thread.request.teslaRequestExecutor
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerProxy
import org.burstsys.fabric.trek.FabricSupervisorParticleTrekMark
import org.burstsys.tesla.scatter.slot.{TeslaScatterSlotState, TeslaScatterSlotZombie}
import org.burstsys.tesla.thread
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import org.burstsys.vitals.logging._

/**
 * used by the scatter gather framework to model a request to assign to a slot
 *
 * @param worker   the worker that the request will run on
 * @param particle the unit of work to be done
 */
final case
class FabricParticleRequest(worker: FabricWorkerProxy, particle: FabricParticle) extends FabricScatteredGatherRequest {

  override def ruid: VitalsUid = newBurstUid

  override def destinationHostName: VitalsHostName = worker.nodeName

  /**
   * if no update is received after this time, the slot is considered tardy
   */
  override def tardyAfter: Duration = 10 seconds

  /**
   * send a message from the supervisor to a single worker/slice (particle). This is async
   * with the return coming in the onComplete which marks the slot's success or failure
   */
  def execute: Future[Unit] = {
    val guid = slot.scatter.guid
    lazy val tag = s"FabricParticleRequest.execute(guid=$guid, ruid=$ruid, host=$destinationHostName)"
    slot.slotBegin()
    FabricSupervisorParticleTrekMark.begin(guid)
    worker.connection.executeParticle(slot, particle) transform {
      case Success(r) =>
        FabricSupervisorParticleTrekMark.end(guid)
        result = r

        /* scatters are closed when they fail or time out. Closing a scatter closes all its requests;
         * closing a request clears out the request's `slot` field. Additionally, only gathers that are received during
         * an active fabric wave will get merged together (and therefore release their resources) */
        slot match {
          // indicates that the request was cancelled
          case null =>
            log info s"$tag releasing resources for timed-out request slot=$slot result=$result"
            result.releaseResourcesOnSupervisor()
          // update outside of active merge loop, will probably never happen since slot probably == null
          case slot if slot.slotState == TeslaScatterSlotZombie =>
            log info s"$tag releasing resources for timed-out request slot=$slot result=$result"
            result.releaseResourcesOnSupervisor()
            /* calling success on a zombie slot doesn't give a success message to the scatter,
             * it removes the slot from the zombie list and marks it as idle */
            slot.slotSuccess()
          case slot =>
            slot.slotSuccess()
        }
        Success((): Unit)

      case Failure(t) =>
        log error burstStdMsg(s"FAB_PARTICLE_REQUEST_FAIL $t $tag", t)
        FabricSupervisorParticleTrekMark.fail(guid)
        if (slot != null) {
          slot.slotFailed(t)
        }
        throw t
    }
  }

  /**
   * cancel this '''scatter request'''
   */
  override def cancel(): Unit = {
    super.cancel()
    // There is not currently a way to cancel the scan on the worker
    // worker.connection.cancelParticleOp(slot.scatter.guid, slot.ruid, particle)
  }
}
