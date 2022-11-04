/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather

import org.burstsys.vitals.logging._

package object plane extends VitalsLogger {

  final val FabricMaxPlanes = 512
/*
  /**
   * consistent handling of resource constraint issues across all types of merges...
   *
   * @param other
   * @return
   */
  final def processResourceConstraints[S <: FabricPlane](self: FabricPlane, other: FabricPlane): Boolean = {
    val dictionaryOverflow = if (self.dictionaryOverflow || other.dictionaryOverflow) {
      self.flagDictionaryOverflow()
      self.clearCollector() // don't want any rows if dictionary gets thrown
      true
    } else false
    val rowLimited = if (self.rowLimitExceeded || other.rowLimitExceeded) {
      true
    } else false
    dictionaryOverflow || rowLimited
  }*/


}
