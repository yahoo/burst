/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore.master

import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplesource.service.SampleSourceMasterService
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreApiRequestInvalidException, SampleStoreDataLocus, SampleStoreGenerator}
import org.burstsys.json.samplestore.configuration.{alloyLociReplicationProperty, alloySkipIndexStreamPropertyKey}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostName, convertHostAddressToHostname, convertLocalAddressToExternal, getLocalHostAddress, getLocalHostName, isIpv4Address}
import org.burstsys.vitals.properties.{VitalsPropertyKey, VitalsPropertyMap, propertyMapToString}
import org.burstsys.vitals.uid.{md5, newBurstUid}

import scala.concurrent.{Future, Promise}

trait JsonSampleSourceMasterService extends SampleSourceMasterService {

  override
  def onSampleStoreDataLocusAdded(hostName: VitalsHostName): Unit = {
    log info burstStdMsg(s"locus added $hostName")
  }

  override
  def onSampleStoreDataLocusRemoved(hostName: VitalsHostName): Unit = {
    log info burstStdMsg(s"locus removed $hostName")
  }

  override
  def getBroadcastVars: Map[VitalsPropertyKey, java.io.Serializable] = {
    var broadcastVars = Map[VitalsPropertyKey, java.io.Serializable]()
    broadcastVars
  }

  override
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource, listenerProperties: VitalsPropertyMap): Future[SampleStoreGenerator] = {
    val promise = Promise[SampleStoreGenerator]
    try {
      val loci: Array[SampleStoreDataLocus] = fetchLoci(mergeProperties(guid, dataSource, listenerProperties))
      val generationMapping: String = loci.map(l => (l.hostAddress, l.partitionProperties(alloySkipIndexStreamPropertyKey)))
        .sortBy(_._2).sortBy(_._1).map(l => s"${l._1}=${l._2}").mkString("\n")
      log debug s"Bucket distribution guid=$guid\n$generationMapping"
      val generationHash = md5(s"$generationMapping refreshedAt=${System.currentTimeMillis()}")

      log info burstStdMsg(s"End view generation for guid=$guid")
      promise.success(
        SampleStoreGenerator(guid, generationHash, loci,
          dataSource.view.schemaName, motifFilter = Some(dataSource.view.viewMotif))
      )
    } catch safely {
      case invalid: SampleStoreApiRequestInvalidException =>
        invalid.printStackTrace()
        promise.failure(invalid)
      case t: Throwable =>
        t.printStackTrace()
        promise.failure(t)
    }
    promise.future
  }

  /**
   * Factor in the need for refresh, probing and modify the loci accordingly
   * Append the merged properties to the loci for the workers to use
   *
   * @param mergedProperties All properties combined
   * @return
   */
  def fetchLoci(mergedProperties: VitalsPropertyMap): Array[SampleStoreDataLocus] = {
    // pretty basic loci lister
    // TODO get loci list from an external provider
    val lociRepeat = Math.max(1, alloyLociReplicationProperty.getOrThrow)

    // list yourself as many times as the repeat property specifies
    val hostName =
      if (isIpv4Address(getLocalHostName))
        convertHostAddressToHostname(convertLocalAddressToExternal(getLocalHostName))
      else getLocalHostName
    val hostAddress = convertLocalAddressToExternal(getLocalHostAddress)
    val port = SampleSourceNexusServer.nexusServer.serverPort

    // put the repeat count in the loci properties so it is passed in the stream
    val preloci = for (i <- 0 until lociRepeat) yield {
      val allProps: VitalsPropertyMap = mergedProperties ++ Map((alloySkipIndexStreamPropertyKey, i.toString))
      SampleStoreDataLocus(newBurstUid, hostAddress, hostName, port, allProps)
    }
    preloci.toArray
  }

  /**
   * Merge all the different properties
   *
   * If there are properties which exist in multiple places
   * The priority will be view > domain > listener > store
   *
   * @param guid               guid
   * @param dataSource         dataSource
   * @param listenerProperties Beast listener Properties
   * @return
   */
  def mergeProperties(
                       guid: String,
                       dataSource: BurstSampleStoreDataSource,
                       listenerProperties: VitalsPropertyMap): VitalsPropertyMap = {
    val mergedProperties =
      dataSource.view.storeProperties ++
        listenerProperties ++
        dataSource.domain.domainProperties ++
        dataSource.view.viewProperties

    log info burstStdMsg(
      s"""
         |****************
         | Merged all properties
         | Priority order: view > domain > listener > store
         | guid=$guid
         | ${propertyMapToString(mergedProperties)}
         |*****************
        """).stripMargin

    mergedProperties
  }
}
