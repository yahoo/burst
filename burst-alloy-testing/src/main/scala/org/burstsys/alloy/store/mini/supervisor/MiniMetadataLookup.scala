/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini.supervisor

import org.burstsys.alloy
import org.burstsys.alloy.store.mini.{MiniView, domainMap, viewMap}
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricMetadataLookup, FabricViewKey, domain}
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.{Failure, Success, Try}

trait MiniMetadataLookup extends AnyRef with FabricMetadataLookup {

  self: MiniStoreSupervisor =>

  final
  def clearMetadata(): Unit = {
    domainMap.clear()
    viewMap.clear()
  }

  /**
   * add as many datasets as you like to the mini store keyed by a Long value
   *
   * @param views
   */
  final
  def addMiniViews(views: Array[MiniView]): this.type = {
    views foreach {
      ds =>
        domainMap += ds.domainKey -> FabricDomain(ds.domainKey)
        viewMap += ds.viewKey -> ds
    }
    this
  }

  /**
   * add as many datasets as you like to the mini store keyed by a Long value
   *
   * @param views
   */
  final
  def withViews[V <: MiniView](views: V*): this.type = {
    views foreach {
      ds =>
        domainMap += ds.domainKey -> domain.FabricDomain(ds.domainKey)
        viewMap += ds.viewKey -> ds
    }
    this
  }

  final
  def withViews[V <: MiniView](views: Array[V]): this.type = {
    views foreach {
      ds =>
        domainMap += ds.domainKey -> domain.FabricDomain(ds.domainKey)
        viewMap += ds.viewKey -> ds
    }
    this
  }

  final
  override
  def domainLookup(key: FabricDomainKey): Try[FabricDomain] = {
    domainMap.get(key) match {
      case None =>
        val msg = s"$burstModuleName domain $key not found"
        log warn msg
        Failure(VitalsException(msg).fillInStackTrace())
      case Some(ds) => Success(ds)
    }
  }

  final
  override
  def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = {
    viewMap.get(key) match {
      case None =>
        val msg = burstStdMsg(s"dataset $key not found")
        log warn msg
        Failure(new RuntimeException(msg).fillInStackTrace())
      case Some(ds) => Success(
        FabricView(domainKey = ds.domainKey, viewKey = ds.viewKey, generationClock = 0, schemaName = ds.schema.name, storeProperties = Map(
          FabricStoreNameProperty -> storeName
        ), viewMotif = "view motif", viewProperties = Map(
          alloy.store.AlloyViewDataPathProperty -> ds.viewKey.toString
        ))
      )
    }
  }

  override
  def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = Success(true)

}
