/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.server.container

import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogRemoteClientConfig
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.json.samplestore.{JsonBrioSampleSourceName, JsonBrioSampleSourceVersion}
import org.burstsys.master.configuration.{burstMasterJsonWatchDirectoryProperty, burstMasterPropertiesFileProperty}
import org.burstsys.relate.RelatePk
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandaloneServer}
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.logging.{VitalsLog, burstStdMsg}

import java.io.File
import java.nio.file.{FileSystems, WatchService}
import scala.util.{Failure, Success}

/**
 */
final case class BurstJsonFileManager( catalog: CatalogService, modality: VitalsServiceModality = VitalsStandaloneServer ) extends VitalsService {

  final val jsonDefaultDomainName = "default_json"

  ////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////
  protected lazy val catalogClient: CatalogService = CatalogService(CatalogRemoteClientConfig)

  protected lazy val watchService: WatchService = FileSystems.getDefault.newWatchService()

  protected var jsonDomain: RelatePk = _

  protected var watchDirectory: File = _

  ////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////


  def setupDefaultMetadata(): Unit = {
    jsonDomain = catalog.ensureDomain(CatalogDomain(0, jsonDefaultDomainName, udk = Some(jsonDefaultDomainName))) match {
      case Success(value) => value
      case Failure(exception) =>
        log warn s"unable to ensure Json default domain $jsonDefaultDomainName exists:  $exception"
        throw exception
    }
  }

  private def scanForJsonFiles(): Unit = {
    val jsonFilesInWatchDir = watchDirectory.listFiles.filter(f => f.getName.endsWith(".json") || f.getName.endsWith(".json.gz"))

    jsonFilesInWatchDir foreach { jf =>
      // look in the catalog
      val name = jf.getName.replaceAll("\\.json(\\.gz)*", "")
      val fileView = CatalogView(pk=0, udk = Some(name), moniker= name, domainFk=jsonDomain, schemaName="quo", storeProperties=
        Map(
          "burst.store.name" -> "sample",
          "burst.samplestore.source.name" -> JsonBrioSampleSourceName,
          "burst.samplestore.source.version" -> JsonBrioSampleSourceVersion,
          "json.samplestore.location" -> s"${jf.getAbsolutePath}"))
      catalog.ensureView(fileView) match {
        case Success(_) =>
          log info s"created json view for file ${jf.getAbsolutePath}"
        case Failure(e) =>
          log warn s"unable to create a json view for file ${jf.getAbsolutePath}: $e"
      }
    }

  }

  def addWatcher(): Unit = {
  }

  def stopWatcher(): Unit = {
  }

  override
  def start: this.type = {
    ensureNotRunning

    // this should be done before any other systems start up
    VitalsLog.configureLogging(burstLog4j2NameProperty.getOrThrow)
    log info startingMessage
    watchDirectory = new File(burstMasterJsonWatchDirectoryProperty.getOrThrow)
    setupDefaultMetadata()
    scanForJsonFiles()
    addWatcher()

    log info burstStdMsg(startedWithDateMessage)

    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info burstStdMsg(stoppedWithDateMessage)
    stopWatcher()
    markNotRunning
    this
  }

  def run: this.type = {
    this
  }
}
