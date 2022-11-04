/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.source

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreDataLocus}
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.annotation.unused

trait TestSourceListener {
  def onGetViewGenerator( guid: String, dataSource: BurstSampleStoreDataSource, topology: SampleStoreTopology, listenerProperties: VitalsPropertyMap ): Unit = {}

  def onFeedStream(@unused stream: NexusStream): Unit = {}

  def onPutBroadcastVars(metadata: MetadataParameters): Unit = {}

  def onGetBroadcastVars(): MetadataParameters = {
    Map.empty
  }

  def onSampleStoreDataLocusRemoved(loci: SampleStoreDataLocus): Unit = {}

  def onSampleStoreDataLocusAdded(loci: SampleStoreDataLocus): Unit = {}
}
