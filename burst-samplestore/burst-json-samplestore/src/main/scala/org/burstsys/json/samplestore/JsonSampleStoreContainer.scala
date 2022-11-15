/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.json.samplestore.configuration.JsonSamplestoreConfiguration
import org.burstsys.json.samplestore.configuration.JsonSamplestoreDefaultConfiguration
import org.burstsys.samplesource.{SampleStoreTopology, SampleStoreTopologyProvider}
import org.burstsys.samplesource.handler.SampleSourceHandlerRegistry
import org.burstsys.samplesource.handler.SimpleSampleStoreApiServerDelegate
import org.burstsys.samplesource.nexus.SampleSourceNexusServer
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.configuration.burstLog4j2NameProperty
import org.burstsys.vitals.healthcheck.VitalsHealthCheckService
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.logging.log
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.git

/**
 */
final case class JsonSampleStoreContainer
  (
    configuration: JsonSamplestoreConfiguration = JsonSamplestoreDefaultConfiguration(),
    modality: VitalsServiceModality
  )
  extends VitalsService
  with SampleStoreTopologyProvider {

  ////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////

  var apiServer: SampleStoreApiServer = SampleStoreApiServer(SimpleSampleStoreApiServerDelegate(this, configuration.properties))

  protected
  var _healthCheck: VitalsHealthCheckService = VitalsHealthCheckService(serviceName)

  ////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    // this should be done before any other systems start up
    VitalsLog.configureLogging(burstLog4j2NameProperty.get)
    log info startingMessage
    log info burstStdMsg(s"BRANCH: '${git.branch}'   COMMIT: '${git.commitId}'  ")

    log info burstStdMsg("starting new nexus server")
    SampleSourceHandlerRegistry.start

    org.burstsys.vitals.reporter.startReporterSystem()

    _healthCheck.start

    // start API server
    log info burstStdMsg("starting sample store api server")
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

  override def getTopology: SampleStoreTopology = {
    // this doesn't know it's topology
    SampleStoreTopology(Iterable.empty)
  }
}
