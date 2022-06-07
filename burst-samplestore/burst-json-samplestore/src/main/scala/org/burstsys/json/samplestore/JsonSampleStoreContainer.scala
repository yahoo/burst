/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.json.samplestore.configuration.{JsonSamplestoreConfiguration, JsonSamplestoreDefaultConfiguration}
import org.burstsys.samplesource.handler.SampleSourceHandler
import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplestore.api.SampleStoreApiService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.configuration.{burstLog4j2NameProperty, burstVitalsHealthCheckPortProperty}
import org.burstsys.vitals.healthcheck.VitalsHealthCheckService
import org.burstsys.vitals.logging.{VitalsLog, burstStdMsg, log}
import org.burstsys.vitals.{VitalsService, git}

/**
 */
final case class JsonSampleStoreContainer(
                                           configuration: JsonSamplestoreConfiguration = JsonSamplestoreDefaultConfiguration(),
                                           modality: VitalsServiceModality
                                    ) extends VitalsService {

  ////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////

  var apiServer: SampleStoreApiService = _

  val _hcPort: Option[Int] = burstVitalsHealthCheckPortProperty.get.orElse(Some(0))

  protected
  var _healthCheck: VitalsHealthCheckService = _

  ////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    // this should be done before any other systems start up
    VitalsLog.configureLogging(burstLog4j2NameProperty.getOrThrow)
    log info startingMessage
    log info burstStdMsg(s"BRANCH: '${git.branch}'   COMMIT: '${git.commitId}'  ")

    log info burstStdMsg("starting new nexus server")
    SampleSourceHandler.start

    org.burstsys.vitals.reporter.startReporterSystem()

    _healthCheck = VitalsHealthCheckService(serviceName).start

    // start API server
    log info burstStdMsg("starting sample store api server")
    apiServer = SampleStoreApiService(modality) talksTo JsonSampleStoreListener(configuration.alloyProperties)
    apiServer.start


    SampleSourceNexusServer.start.nexusServer
    log info burstStdMsg(startedWithDateMessage)

    markRunning
    this
  }


  override
  def stop: this.type = {
    ensureRunning
    apiServer.stop
    log info burstStdMsg(stoppedWithDateMessage)
    SampleSourceNexusServer.stop
    markNotRunning
    this
  }

  def run: this.type = {
    this
  }
}
