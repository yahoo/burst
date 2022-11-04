/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, BurstSampleStoreDomain, BurstSampleStoreView, SampleStoreDataLocus}

import scala.language.implicitConversions

package object model {

  implicit
  def fabricToSampleStoreDataSource(fabricDataSource: FabricDatasource): BurstSampleStoreDataSource = {
    import fabricDataSource._
    BurstSampleStoreDataSource(
      domain = BurstSampleStoreDomain(
        domainKey = domain.domainKey,
        domainProperties = domain.domainProperties
      ),
      view = BurstSampleStoreView(
        viewKey = view.viewKey,
        schemaName = view.schemaName,
        viewMotif = view.viewMotif,
        storeProperties = view.storeProperties,
        viewProperties = view.viewProperties
      )
    )
  }

  implicit def locusToRddLocus(locus: SampleStoreDataLocus): SampleStoreLocus = {
    SampleStoreLocus(locus.suid, locus.hostName, locus.hostAddress, locus.port, locus.partitionProperties)
  }

}
