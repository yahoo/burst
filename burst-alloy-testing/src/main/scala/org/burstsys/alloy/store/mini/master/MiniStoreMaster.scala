/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini.master

import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.data.master.store.FabricStoreMaster
import org.burstsys.fabric.data.model.slice.FabricSlice
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.metadata._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.alloy.store.mini
import org.burstsys.alloy.store.mini.sliceSet
import org.burstsys.vitals.uid._

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * A specialized 'mini' Fabric store that supports the creation of small targeted datasets that can be
 * used for semantic/unit/performance tests
 */
final case
class MiniStoreMaster(container: FabricMasterContainer) extends FabricStoreMaster
  with MiniMetadataLookup with MiniSlicer {

  override lazy val storeName: FabricStoreName = mini.MiniStoreName

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    clearMetadata()
    markNotRunning
    this
  }
}
