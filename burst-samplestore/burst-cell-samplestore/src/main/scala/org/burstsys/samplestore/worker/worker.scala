/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.fabric.wave.execution.FabricLoadEvent
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid

import scala.language.postfixOps
import scala.concurrent.duration._

/**
  * spark independent part of sample store worker side processing
  */
package object worker extends VitalsLogger {

  final val releaseStreamTimeout = 2 minutes

  final val collectStreamTimeout: Duration = 15 seconds

  final val TimeoutLimit = 3

  final case class ParticleStreamsAcquired(override val guid: VitalsUid) extends FabricLoadEvent("samplestore", "acquired-streams")

  final case class ParticleStreamsFinished(override val guid: VitalsUid) extends FabricLoadEvent("samplestore", "finished-streams")

  final case class ParticleWritesFinished(override val guid: VitalsUid) extends FabricLoadEvent("samplestore", "finished-writes")

}
