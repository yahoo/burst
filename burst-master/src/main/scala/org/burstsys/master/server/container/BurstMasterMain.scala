/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.server.container

import org.burstsys._
import org.burstsys.fabric.container.MasterLog4JPropertiesFileName
import org.burstsys.json.samplestore.JsonSampleStoreContainer
import org.burstsys.json.samplestore.configuration.{JsonSamplestoreDefaultConfiguration, JsonSamplestoreDistributedConfiguration}
import org.burstsys.master.configuration.burstMasterPropertiesFileProperty
import org.burstsys.vitals.VitalsService.{VitalsStandaloneServer, VitalsStandardServer}
import org.burstsys.vitals.io.loadSystemPropertiesFromJavaPropertiesFile
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.metrics.VitalsMetricsRegistry

object BurstMasterMain {

  final case class BurstMasterArguments(
                                         standalone: Boolean = false,
                                         cellWorkers: Int = 5,
                                         useTls: Boolean = false,
                                         keyPath: String = "",
                                         certPath: String = "",
                                         caChain: String = "",
                                         jsonSampleStore: Boolean = false
                                       )

  def main(args: Array[String]): Unit = {

    val defaultArguments = BurstMasterArguments()

    val parser = new scopt.OptionParser[BurstMasterArguments]("BurstMasterMain") {
      opt[Unit]('s', "standalone")
        .text(s"start in standalone development mode (default '${defaultArguments.standalone}')")
        .maxOccurs(1)
        .action {
          case (_, arguments) => arguments.copy(standalone = true)
        }
      opt[Int]('w', "cell-workers")
        .text(s"number of workers to start in standalone mode (default ${defaultArguments.cellWorkers}')")
        .maxOccurs(1)
        .action {
          case (workers, arguments) => arguments.copy(cellWorkers = workers)
        }
      opt[Unit]('t', "useTLS")
        .text(s"use tls for all network communication. Only intended for local development, requires having local x509 service certs (default '${defaultArguments.useTls}')")
        .maxOccurs(1)
        .action {
          case (_, arguments) => arguments.copy(useTls = true)
        }
      opt[String]("keyPath")
        .text("the path to the private key for the application's certificate")
        .maxOccurs(1)
        .action {
          case (path, arguments) => arguments.copy(keyPath = path)
        }
      opt[String]("certPath")
        .text("the path to the application's public certificate")
        .maxOccurs(1)
        .action {
          case (path, arguments) => arguments.copy(certPath = path)
        }
      opt[String]("caChain")
        .text("the path to the application's certificate authority chain")
        .maxOccurs(1)
        .action {
          case (path, arguments) => arguments.copy(caChain = path)
        }
      opt[Unit]("jsonSampleStore")
        .text("toggle the local json sample store on")
        .maxOccurs(1)
        .action {
          case (_, arguments) ⇒ arguments.copy(jsonSampleStore = true)
        }
      help("help")
    }

    parser.parse(args.toSeq, defaultArguments) match {
      case None =>
        parser.showUsageAsError()
        System.exit(-1)
      case Some(arguments) =>
        /**
         * first get a set of basic config properties - this will eventually just be catalog DB connection info...
         */
        loadSystemPropertiesFromJavaPropertiesFile(burstMasterPropertiesFileProperty.getOrThrow)
        if (arguments.standalone) {
          runLocally(arguments)
        } else {
          runInCluster(arguments)
        }
    }
  }

  def runLocally(args: BurstMasterArguments): Unit = {
    vitals.configuration.burstHomeProperty.set(System.getProperty("user.home"))
    VitalsLog.configureLogging(MasterLog4JPropertiesFileName)
    vitals.configuration.burstVitalsEnableReporting.set(false)
    vitals.git.turnOffBuildValidation()

    fabric.configuration.burstFabricMasterStandaloneProperty.set(true)
    fabric.configuration.burstFabricWorkerStandaloneProperty.set(true)

    catalog.configuration.burstCatalogCannedImportStandaloneOnlyProperty.set(true)

    if (args.useTls) {
      vitals.configuration.burstSslKeyPath.set(args.keyPath)
      vitals.configuration.burstSslCertPath.set(args.certPath)
      vitals.configuration.burstTrustedCaPath.set(args.caChain)
      vitals.configuration.burstEnableCompositeTrust.set(true)

      // thrift
      catalog.configuration.burstCatalogApiSslEnableProperty.set(true)
      agent.configuration.burstAgentApiSslEnableProperty.set(true)
      samplestore.api.configuration.burstSampleStoreApiSslEnableProperty.set(true)

      // netty
      nexus.configuration.burstNexusSslEnableProperty.set(true)
      // fabric.configuration.burstFabricSslEnableProperty.set(true) // this doesn't exist yet
    }

    val master = fabric.container.masterContainer

    val alloyContainer: JsonSampleStoreContainer =
      if (args.jsonSampleStore)
        JsonSampleStoreContainer(JsonSamplestoreDefaultConfiguration(), VitalsStandaloneServer)
      else
        null

    master.start
    if (alloyContainer != null) {
      log info s"Starting alloy sample store process"
      alloyContainer.start
    }

    log info s"Starting ${args.cellWorkers} workers"
    val workers = (1 to args.cellWorkers).par.map(id => {
      Thread.sleep(id * 100)
      val worker = fabric.container.getWorkerContainer(id, 0)
      worker.start
    }).toArray

    VitalsMetricsRegistry.disable()

    master.run.stop
    workers.foreach(_.stopIfNotAlreadyStopped)
    if (alloyContainer != null)
      alloyContainer.stop
  }

  def runInCluster(args: BurstMasterArguments): Unit = {
    VitalsLog.configureLogging(MasterLog4JPropertiesFileName)
    val alloyContainer: JsonSampleStoreContainer =
      if (args.jsonSampleStore)
        JsonSampleStoreContainer(JsonSamplestoreDistributedConfiguration(), VitalsStandardServer)
       else
        null

    val master = fabric.container.masterContainer.start
    if (alloyContainer != null)
      alloyContainer.start
    master.run.stop
    if (alloyContainer != null)
      alloyContainer.start
  }

}
