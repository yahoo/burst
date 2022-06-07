/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.catalog.model.master._
import org.burstsys.catalog.model.worker._
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricMetadataLookup, FabricViewKey, domain, view}
import org.burstsys.fabric.topology.model.node.master.FabricMaster
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.vitals.errors._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.{Failure, Success, Try}
import org.burstsys.vitals.logging._

/**
 * This is a version of the [[FabricMetadataLookup]] implemented using the Burst Catalog
 */
private[catalog] final case
class CatalogMetadataLookup(catalog: CatalogService) extends AnyRef with FabricMetadataLookup {

  override
  def domainLookup(key: FabricDomainKey): Try[FabricDomain] = {
    resultOrFailure {
      catalog.findDomainByPk(key) map {
        d => FabricDomain(d.pk, d.domainProperties)
      }
    }
  }

  override
  def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = {
    resultOrFailure {
      catalog.findViewByPk(key) map {
        v => FabricView(v.domainFk, v.pk, v.generationClock, v.schemaName, v.viewMotif, v.viewProperties, v.storeProperties)
      }
    }
  }

  override
  def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = {
    val propList = updatedProperties.map(e => s"${e._1.replace(".", "_")}=${e._2}").mkString("\n\t", ",\n\t", "")
    log info s"VIEW_LOAD_RECORD CatalogMetadataLookup.recordViewLoad(generation=$key) $propList"
    resultOrFailure {
      catalog.recordViewLoad(key.viewKey, updatedProperties) map { _ => true }
    }
  }

  override
  def masterLookup(moniker: String): Try[FabricMaster] = {
    resultOrFailure {
      catalog.findMasterByMoniker(moniker) map {
        m => FabricMaster(m.pk, m.nodeName, m.nodeAddress, m.masterPort, m.masterProperties)
      }
    }
  }

  override
  def masterRegistration(cellMoniker: String, nodeMoniker: String, nodeName: VitalsHostName, nodeAddress: VitalsHostAddress): Try[FabricMaster] = {
    // TODO add times to this
    resultOrFailure {
      val masterPort = 0
      val (cellPk: Long, sitePk: Long) = catalog.findCellByMoniker(cellMoniker) match {
        case Failure(mt) => return Failure(mt) // this is unrecoverable for registration
        case Success(cell) => (cell.pk, cell.siteFk)

      }
      catalog.findMasterByMoniker(nodeMoniker) flatMap {
        existing => catalog.updateMaster(existing.copy(nodeName = nodeName, nodeAddress = nodeAddress, masterPort = masterPort))
      } recoverWith { case _ =>
        val master = CatalogMaster(0, nodeMoniker, nodeName, nodeAddress, masterPort, sitePk, Some(cellPk))
        catalog.insertMaster(master) map {
          pk => FabricMaster(pk, master.nodeName, master.nodeAddress, master.masterPort, master.masterProperties)
        }
      }
    }
  }

  override def workerLookup(pk: FabricDomainKey): Try[FabricWorker] = {
    resultOrFailure {
      catalog.findWorkerByPk(pk) map {
        w => FabricWorker(w.pk, w.moniker, w.nodeName, w.nodeAddress, w.workerProperties)
      }
    }
  }

  override
  def workerLookup(moniker: String): Try[FabricWorker] = {
    resultOrFailure {
      catalog.findWorkerByMoniker(moniker) map {
        w => FabricWorker(w.pk, w.moniker, w.nodeName, w.nodeAddress, w.workerProperties)
      }
    }
  }

  override
  def workerRegistration(cellMoniker: String, nodeMoniker: String, nodeName: VitalsHostName, nodeAddress: VitalsHostAddress): Try[FabricWorker] = {
    // TODO add times to this
    resultOrFailure {
      val (cellPk: Long, sitePk: Long) = catalog.findCellByMoniker(cellMoniker) match {
        case Failure(mt) => return Failure(mt) // this is unrecoverable for registration
        case Success(cell) => (cell.pk, cell.siteFk)

      }
      catalog.findWorkerByMoniker(nodeMoniker) flatMap {
        existing => catalog.updateWorker(existing.copy(nodeName = nodeName, nodeAddress = nodeAddress))
      } recoverWith { case _ =>
        val worker = CatalogWorker(0, nodeMoniker, nodeName, nodeAddress, sitePk, Some(cellPk))
        catalog.insertWorker(worker) map {
          pk => FabricWorker(pk, worker.moniker, worker.nodeName, worker.nodeAddress, worker.workerProperties)
        }
      }
    }
  }

}
