/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.vitals.logging._

/**
 * this package contains code that is involved in the
 * compilation and caching of code generated artifacts ([[org.burstsys.felt.model.sweep.FeltSweep]] and
 * [[FeltTraveler]].
 */
package object compile extends VitalsLogger {

  type FeltClassName = String
  type FeltByteCode = Array[Byte]

  type FeltArtifactInstance = Any

  type FeltClassSpec = (FeltClassName, FeltByteCode)
  type FeltClassSpecList = Array[FeltClassSpec]

}
