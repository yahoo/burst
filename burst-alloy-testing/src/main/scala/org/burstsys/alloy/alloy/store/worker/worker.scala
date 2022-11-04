/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store

import org.burstsys.fabric.wave.execution.FabricLoadEvent
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.uid.VitalsUid

package object worker extends VitalsLogger {

  private[alloy] final val buffersPerParcel = 5

  private[alloy] final val bufferSize: Int = 2e7.toInt
  private[alloy] final val blobSize: Int = 2e7.toInt

  final case class ParticleGotFile(guid: VitalsUid) extends FabricLoadEvent("canned", "got-alloy-file")

  final case class ParticleReadFile(guid: VitalsUid) extends FabricLoadEvent("canned", "read-alloy-file")

  final case class ParticleWroteSlice(guid: VitalsUid) extends FabricLoadEvent("canned", "wrote-slice-file")
}
