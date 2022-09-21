/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.brio.flurry.provider.quo
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.container.master.FabricMasterContainer
import org.burstsys.fabric.container.worker.FabricWorkerContainer
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.store.FabricStoreName
import org.burstsys.fabric.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.data.model.store.FabricStoreProvider
import org.burstsys.fabric.metadata.model
import org.burstsys.fabric.metadata.model._
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.util.Success
import scala.util.Try

/**
 * mock store stuff for unit tests
 */
package object mock extends VitalsLogger {

  val itemsPerSlice = 500

  final val MockStoreName = "mock"

  var currentGenerationHash: String = "NO_HASH"

  /**
   * mock store plugin provider
   */
  final case class MockStoreProvider() extends FabricStoreProvider[MockStoreMaster, MockStoreWorker] {

    val storeName: String = mock.MockStoreName

    val masterClass: Class[MockStoreMaster] = classOf[MockStoreMaster]

    val workerClass: Class[MockStoreWorker] = classOf[MockStoreWorker]

  }


  var mockWorker: FabricWorkerContainer = _

  val domain: FabricDomain = model.domain.FabricDomain(domainKey = 5)
  val view: FabricView = model.view.FabricView(
    domainKey = domain.domainKey, viewKey = 6, generationClock = 0,
    storeProperties = Map("burst.store.name" -> "mock")
  )

  val mockDatasource: FabricDatasource = model.datasource.FabricDatasource(domain, view)

  var mockMaster: FabricMasterContainer = _

  val domainKey: FabricDomainKey = 1L
  val viewKey: FabricViewKey = 1L
  val generationClock: FabricGenerationClock = 0L

  def mockMetadata(schemaName: BrioSchemaName, storeName: FabricStoreName,
                   storePath: String): (FabricDatasource, FabricMetadataLookup) = {

    val domain = FabricDomain(domainKey)

    val storeProperties: VitalsPropertyMap = Map(
      FabricStoreNameProperty -> storeName
    )

    val viewMotif: String = "some random motif string"

    val viewProperties: VitalsPropertyMap = Map()

    val view = model.view.FabricView(domainKey = domainKey, viewKey = viewKey,
      generationClock = generationClock, schemaName = schemaName, storeProperties = storeProperties,
      viewMotif = viewMotif, viewProperties = viewProperties
    )

    val datasource = model.datasource.FabricDatasource(domain, view)

    (datasource, new FabricMetadataLookup {

      override def domainLookup(key: FabricDomainKey): Try[FabricDomain] = {
        Success(domain)
      }

      override def viewLookup(key: FabricViewKey, validate: Boolean): Try[FabricView] = {
        Success(view)
      }

      override def recordViewLoad(key: FabricGenerationKey, updatedProperties: VitalsPropertyMap): Try[Boolean] = Success(true)

    })
  }

}
