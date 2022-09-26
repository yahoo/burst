/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.metadata.model

import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.Try

/**
 * These are low level Fabric layer metadata operations that need to be implemented
 * at a higher layer of the system e.g. the cell supervisor, that has access to a persistent
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

  final
  def datasource(over: FabricOver, validate: Boolean): FabricDatasource = {
    FabricMetadataReporter.recordDomainLookup()
    val domain = domainLookup(over.domainKey).getOrElse(throw VitalsException(s"domain ${over.domainKey} not found"))
    FabricMetadataReporter.recordViewLookup()
    val view = viewLookup(over.viewKey, validate).getOrElse(throw VitalsException(s"view ${over.viewKey} not found"))
    FabricDatasource(domain, view)
  }

}
