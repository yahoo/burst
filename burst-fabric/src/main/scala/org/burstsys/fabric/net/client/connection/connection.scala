/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import org.burstsys.fabric.execution.model.pipeline.FabricPipelineEvent
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import scala.language.postfixOps

package object connection extends VitalsLogger {

  abstract class FabricExecutionEvent(val eventId: Long) extends FabricPipelineEvent {
    private val _nanos: Long = System.nanoTime
    def nanos: Long = _nanos
  }

  abstract class FabricLoadEvent(val store: String, val event: String) extends FabricExecutionEvent(200)

  final case class ParticleExecutionStart(guid: VitalsUid) extends FabricExecutionEvent(100)

  final case class ParticleExecutionDataReady(guid: VitalsUid) extends FabricExecutionEvent(300)

  final case class ParticleExecutionFinished(guid: VitalsUid) extends FabricExecutionEvent(400)

}
