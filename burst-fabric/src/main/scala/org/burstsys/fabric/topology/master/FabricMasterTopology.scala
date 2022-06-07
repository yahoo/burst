/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.topology.master

import org.burstsys.fabric.container.FabricMasterService
import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.net.server.FabricNetServerListener
import org.burstsys.fabric.topology.model.node.worker.{FabricWorkerNode, FabricWorkerProxy}
import org.burstsys.vitals.VitalsService.VitalsServiceModality

/**
  * Maintain a ''topology'' of active workers in various states of dynamic ''health'' on the ''master''
  * This involved the following elements:
  * <ol>
  * <li>'''Tethering:''' [[org.burstsys.fabric.net.client.FabricNetClient]] (`worker`) regularly sends an
  * async ''tether'' message to [[org.burstsys.fabric.net.server.FabricNetServer]] (master) that indicates
  * its alive. This acts as a heartbeat from worker to master that is evaluated by both. </li>
  * <li>'''Assessing:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`master`) regularly sends a
  * sync (RPC) ''assess'' request message to [[org.burstsys.fabric.net.client.FabricNetClient]] (`worker`) requesting
  * health or load stats which is returned as an ''assess'' response.
  * This acts as a heartbeat from master to worker that is evaluated only on the master.
  * Both the contents of the assessment ''and'' the speed and reliability of the response are used as status info</li>
  * <li>'''Ranking:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`master`) dynamically
  * updates its list of active workers and assigns health ranking and
  * moves workers from state to state in response to incoming tethers, and assessments</li>
  * <li>'''Lasso:''' [[org.burstsys.fabric.net.server.FabricNetServer]] (`master`) chooses sets of workers
  * for loads/waves based on locality and rank </li>
  * </ol>
  */
trait FabricMasterTopology extends FabricMasterService {

  /**
    * return a set of healthy, least busy workers
    */
  def bestWorkers(count: Int): Array[FabricWorkerNode]

  /**
    * returns a set of all workers
    */
  def allWorkers: Array[FabricWorkerNode]

  /**
    * return a set of all healthy workers
    */
  def healthyWorkers: Array[FabricWorkerNode]

  /**
    * get a worker connection given a key
    */
  def getWorker(key: FabricWorkerNode, mustBeConnected: Boolean = true): Option[FabricWorkerProxy]

  /**
    * wire up a listener for topology events
    */
  def talksTo(listeners: FabricTopologyListener*): this.type

}

object FabricMasterTopology {

  def apply(container: FabricMasterContainer): FabricMasterTopology =
    FabricMasterTopologyContext(container: FabricMasterContainer)

}

private final case
class FabricMasterTopologyContext(container: FabricMasterContainer)
  extends FabricMasterTopology with FabricNetServerListener with FabricTopologyTender {

  override val serviceName: String = s"fabric-topology-service"

  override def modality: VitalsServiceModality = container.bootModality

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    container.netServer talksTo this
    this talksTo FabricTopologyReporter
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    markNotRunning
    this
  }

}
