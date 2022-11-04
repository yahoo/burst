/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.data.supervisor.store.FabricStoreSupervisor
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.store.FabricStoreName
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

import scala.concurrent.Future
import scala.language.postfixOps


/**
 * Store to be used in unit tests
 */
final case
class MockStoreSupervisor(container: FabricWaveSupervisorContainer) extends FabricStoreSupervisor with MockSlicer {

  var workers: Array[FabricWorkerNode] = _

  override lazy val storeName: FabricStoreName = MockStoreName

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    this
  }

  override
  def stop: this.type = {
    this
  }


}
