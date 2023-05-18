/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.wave.exception.FabricException
import org.burstsys.fabric.wave.execution.model.pipeline.FabricPipelineEvent
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

package object execution extends VitalsLogger {

  /**
   * type that has releasable resources on either the
   * worker or supervisor sides
   */
  trait FabricResourceHolder extends Any {

    /**
     * release resources on the supervisor side
     */
    def releaseResourcesOnSupervisor(): Unit = {}

    /**
     * release resources on the worker side
     */
    def releaseResourcesOnWorker(): Unit = {}

  }

  abstract class FabricExecutionEvent(val eventId: Long) extends FabricPipelineEvent {
    private val _nanos: Long = System.nanoTime
    def nanos: Long = _nanos
  }

  abstract class FabricLoadEvent(val store: String, val event: String) extends FabricExecutionEvent(200)

  final case class ParticleExecutionStart(guid: VitalsUid) extends FabricExecutionEvent(100)

  final case class ParticleExecutionDataReady(guid: VitalsUid) extends FabricExecutionEvent(300)

  final case class ParticleExecutionFinished(guid: VitalsUid) extends FabricExecutionEvent(400)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DEPRECATED
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * a [[FabricException]] exception originating from an '''execution''' aspect of fabric pipelines
   *
   * @deprecated TODO this is not used anywhere except one unit test...
   */
  final class FabricExecutionException(message: String, cause: Option[Throwable] = None) extends FabricException(message, cause)

  object FabricExecutionException {
    def apply(message: String): FabricExecutionException = new FabricExecutionException(message, None)
  }

}
