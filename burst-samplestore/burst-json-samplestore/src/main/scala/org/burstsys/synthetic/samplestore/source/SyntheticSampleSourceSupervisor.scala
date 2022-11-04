/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplesource.service.SampleSourceSupervisorService
import org.burstsys.samplestore.api
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreDataLocus, SampleStoreGeneration}
import org.burstsys.synthetic.samplestore.configuration
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid.newBurstUid

import scala.concurrent.Future

case class SyntheticSampleSourceSupervisor() extends SampleSourceSupervisorService {

  /**
   * @return The name of this sample source supervisor.
   */
  override def name: String = SyntheticSampleSourceName

  /**
   * Figures out the loci and other partition properties
   * required by the burst cell to make subsequent sample source queries
   * to the sample source worker
   */
  override def getViewGenerator(
                                 guid: String,
                                 dataSource: BurstSampleStoreDataSource,
                                 topology: SampleStoreTopology,
                                 listenerProperties: VitalsPropertyMap
                               ): Future[api.SampleStoreGeneration] = {
    TeslaRequestFuture {
      val properties = mergeProperties(dataSource, listenerProperties)
      val extended = properties.extend
      val hashIsInvariant = extended.getValueOrProperty(configuration.defaultPersistentHashProperty)
      val hash = if (hashIsInvariant) InvariantHash else newBurstUid
      val loci = for (l <- topology.loci) yield SampleStoreDataLocus(newBurstUid, l.hostAddress, l.hostName, l.port, properties)
      SampleStoreGeneration(guid, hash, loci.toArray, dataSource.view.schemaName, Some(dataSource.view.viewMotif))
    }
  }

  override def onSampleStoreDataLocusRemoved(locus: SampleStoreDataLocus): Unit = {
    log info burstStdMsg(s"removed worker=$locus")
  }

  override def onSampleStoreDataLocusAdded(locus: SampleStoreDataLocus): Unit = {
    log info burstStdMsg(s"added worker=$locus")
  }
}
