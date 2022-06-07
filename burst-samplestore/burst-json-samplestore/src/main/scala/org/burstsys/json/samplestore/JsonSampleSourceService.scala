/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.brio.provider._
import org.burstsys.json.samplestore.master.JsonSampleSourceMasterService
import org.burstsys.json.samplestore.worker.JsonSampleSourceWorkerService
import org.burstsys.samplesource.SampleSourceId
import org.burstsys.samplesource.service.SampleSourceService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardServer}

class JsonSampleSourceService(override val modality: VitalsServiceModality) extends SampleSourceService
  with JsonSampleSourceMasterService with JsonSampleSourceWorkerService {

  def this() = this(VitalsStandardServer)

  override def id: SampleSourceId = JsonBrioSampleSourceId

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    loadBrioSchemaProviders()
    log info startedMessage
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    log info stoppedMessage
    this
  }
}
