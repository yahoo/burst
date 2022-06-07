/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model

import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.topology.model.node.master.FabricMaster
import org.burstsys.fabric.topology.model.node.worker.FabricWorker
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName}
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.Try

/**
 * These are low level Fabric layer metadata operations that need to be implemented
 * at a higher layer of the system e.g. the cell master, that has access to a persistent
 * metadata store, e.g. the catalog
 */
trait FabricMetadataLookup extends Any {

  /**
   * find the fabric domain information associated with a domain key
   *
   * @param key
   * @return
   */
  def domainLookup(key: FabricDomainKey): Try[FabricDomain]

  /**
   * find the fabric view information associated with a view key
   *
   * @param key
   * @return
   */
  def viewLookup(key: FabricViewKey, validate: Boolean = false): Try[FabricView]

  /**
   * record load & update the view with the updatedProperties
   *
   * @param key
   * @param updatedProperties
   * @return
   */
  def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean]

  /**
   * lookup a master by primary key
   *
   * @param masterPk
   * @return
   */
  def masterLookup(masterPk: Long): Try[FabricMaster] = ???

  /**
   * register a master in the catalog
   *
   * @return
   */
  def masterRegistration(cellMoniker: String, nodeMoniker: String, name: VitalsHostName, address: VitalsHostAddress): Try[FabricMaster] = ???

  /**
   * lookup a master by moniker
   *
   * @param moniker
   * @return
   */
  def masterLookup(moniker: String): Try[FabricMaster] = ???

  /**
   * lookup a worker by primary key
   *
   * @param pk
   * @return
   */
  def workerLookup(pk: Long): Try[FabricWorker] = ???

  /**
   * lookup a worker by moniker
   *
   * @param moniker
   * @return
   */
  def workerLookup(moniker: String): Try[FabricWorker] = ???

  /**
   * register a worker in the catalog
   *
   * @return
   */
  def workerRegistration(cellMoniker: String, nodeMoniker: String, name: VitalsHostName, address: VitalsHostAddress): Try[FabricWorker] = ???

  /**
   * lookup workers assigned to a master by primary key
   *
   * @param pk
   * @return
   */
  def workersForMasterLookup(pk: Long): Try[Array[FabricWorker]] = ???

  final
  def datasource(over: FabricOver, validate: Boolean): FabricDatasource = {
    FabricMetadataReporter.recordDomainLookup()
    val domain = domainLookup(over.domainKey).getOrElse(throw VitalsException(s"domain ${over.domainKey} not found"))
    FabricMetadataReporter.recordViewLookup()
    val view = viewLookup(over.viewKey, validate).getOrElse(throw VitalsException(s"view ${over.viewKey} not found"))
    FabricDatasource(domain, view)
  }

}
