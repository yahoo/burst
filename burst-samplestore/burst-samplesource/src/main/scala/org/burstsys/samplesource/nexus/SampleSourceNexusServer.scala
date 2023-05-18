/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.nexus

import org.burstsys.nexus
import org.burstsys.nexus.server.NexusServer
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}
import org.burstsys.vitals.net._

object SampleSourceNexusServer extends VitalsService {

  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = {
    s"sample-source-nexus-server(${
      if(_nexusServer == null) "" else s"host=${_nexusServer.serverHost} port=${_nexusServer.serverPort}"
    })"
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _nexusServer: NexusServer = _

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // accessor
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def nexusServer: NexusServer = {
    ensureRunning
    _nexusServer
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecyle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    val feeder = SampleSourceNexusFeeder()
    org.burstsys.vitals.sysinfo.SystemInfoService.registerComponent(feeder)
    _nexusServer = nexus.grabServer(getPublicHostAddress) fedBy feeder
    log info startedMessage
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    nexus.releaseServer(_nexusServer)
    markNotRunning
    this
  }

}
