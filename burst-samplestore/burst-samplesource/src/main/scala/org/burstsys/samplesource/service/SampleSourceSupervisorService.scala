/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties._

import scala.concurrent.Future

trait SampleSourceSupervisorService extends VitalsService {

  override def modality: VitalsService.VitalsServiceModality = VitalsStandardServer

  override def start: this.type = {
    log info startingMessage
    markRunning
  }

  override def stop: this.type = {
    log info stoppingMessage
    markNotRunning
  }

  /**
   * @return The name of this sample source supervisor. Should probably match the name in the [[SampleSourceService]]
   */
  def name: String

  /**
   * Figures out the loci and other partition properties required by the burst cell
   * to load data from the sample source workers
   *
   * @param guid               a unique id for this request (also passed to sample source workers on load)
   * @param dataSource         the domain/view to be loaded
   * @param listenerProperties server-side properties passed to the view generator
   * @return a future that resolves to the generation for the cell to load
   */
  def getViewGenerator(guid: String,
                       dataSource: BurstSampleStoreDataSource,
                       listenerProperties: VitalsPropertyMap): Future[SampleStoreGeneration]


  /**
   * Merge all the different properties
   *
   * If there are properties which exist in multiple places they will be merged with the
   * priority of viewProperties > domainProperties > listenerProperties > storeProperties.
   * So that `view.viewProperties` is weighted the most and `view.storeProperties` is weighted the least
   *
   * @param dataSource         the datasource for which we are generating a view
   * @param listenerProperties the properties passed to the view generator
   * @return
   */
  def mergeProperties(
                       dataSource: BurstSampleStoreDataSource,
                       listenerProperties: VitalsPropertyMap = Map.empty): VitalsPropertyMap = {
    dataSource.view.storeProperties ++
      listenerProperties ++
      dataSource.domain.domainProperties ++
      dataSource.view.viewProperties
  }

  /**
   * Variables to be broadcast from the sample source supervisor to the sample source workers.
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
   * The action to be performed on the supervisor when a locus goes offline
   *
   * @param hostName
   */
  def onSampleStoreDataLocusRemoved(hostName: VitalsHostName): Unit = {}

  /**
   * The action to be performed on the supervisor when a locus comes online
   *
   * @param hostName
   */
  def onSampleStoreDataLocusAdded(hostName: VitalsHostName): Unit = {}

}
