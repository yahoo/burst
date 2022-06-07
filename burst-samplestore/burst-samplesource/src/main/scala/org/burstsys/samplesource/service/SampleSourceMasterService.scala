/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreDataLocus, SampleStoreGenerator}
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties._

import scala.concurrent.Future
import scala.util.Try

trait SampleSourceMasterService extends Any {

  /**
    * Figures out the loci and other partition properties
    * required by the burst cell to make subsequent sample source queries
    * to the sample source worker
    *
    * @param guid
    * @param dataSource
    * @param listenerProperties
    * @return
    */
  def getViewGenerator(guid: String,
                       dataSource: BurstSampleStoreDataSource,
                       listenerProperties: VitalsPropertyMap): Future[SampleStoreGenerator]

  /**
    * Variables to be broadcast from the sample source master to the sample source workers.
    * This could be for time-consuming computations or for avoiding multiple
    * simultaneous connections to a shared resource like a db
    *
    * If left empty, nothing should be broadcast
    *
    * Caution: multiple SampleSource instances could potentially have the same key which would
    * lead to erroneous behavior
    *
    * @return
    */
  def getBroadcastVars: Map[VitalsPropertyKey, java.io.Serializable] = Map.empty

  /**
    * The action to be performed on the master when a locus goes offline
    *
    * @param hostName
    */
  def onSampleStoreDataLocusRemoved(hostName: VitalsHostName): Unit = {}

  /**
    * The action to be performed on the master when a locus comes online
    *
    * @param hostName
    */
  def onSampleStoreDataLocusAdded(hostName: VitalsHostName): Unit = {}

}
