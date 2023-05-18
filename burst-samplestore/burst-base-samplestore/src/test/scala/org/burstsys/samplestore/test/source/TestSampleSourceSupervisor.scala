/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.source

import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplesource.service.{MetadataParameters, SampleSourceSupervisorService}
import org.burstsys.samplestore.api
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreDataLocus, SampleStoreGeneration}
import org.burstsys.samplestore.test.configuration
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.Future

case class TestSampleSourceSupervisor() extends SampleSourceSupervisorService {

  /**
   * @return The name of this sample source supervisor.
   */
  override def name: String = TestSampleSourceName

  /**
   * Figures out the loci and other partition properties
   * required by the burst cell to make subsequent sample source queries
   * to the sample source worker
   *
   */
  override def getViewGenerator(
                                 guid: String,
                                 dataSource: BurstSampleStoreDataSource,
                                 topology: SampleStoreTopology,
                                 listenerProperties: VitalsPropertyMap
                               ): Future[api.SampleStoreGeneration] = {
    testListener.foreach(_.onGetViewGenerator(guid, dataSource, topology, listenerProperties))
    TeslaRequestFuture {
      val properties = mergeProperties(dataSource, listenerProperties)
      val extended = properties.extend
      val lociCount = extended.getValueOrProperty(configuration.defaultLociCountProperty)
      val (host, addr) = ("localhost", "127.0.0.1")
      val loci = for (i <- 1 to lociCount) yield SampleStoreDataLocus(newBurstUid, addr, host, i, properties)
      val hash = InvariantHash

      SampleStoreGeneration(guid, hash, loci.toArray, dataSource.view.schemaName, Some(dataSource.view.viewMotif))
    }
  }

  override def getBroadcastVars: MetadataParameters = {
    if (testListener.isEmpty)
      Map.empty
    else
      testListener.get.onGetBroadcastVars()
  }

  /**
   * The action to be performed on the supervisor when a locus goes offline
   */
  override def onSampleStoreDataLocusRemoved(locus: SampleStoreDataLocus): Unit = {
    testListener.foreach(_.onSampleStoreDataLocusRemoved(locus))
  }

  /**
   * The action to be performed on the supervisor when a locus comes online
   */
  override def onSampleStoreDataLocusAdded(locus: SampleStoreDataLocus): Unit = {
    testListener.foreach(_.onSampleStoreDataLocusAdded(locus))
  }
}
