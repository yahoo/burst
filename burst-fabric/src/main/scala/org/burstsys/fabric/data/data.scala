/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.exception.FabricException
import org.burstsys.vitals.logging._

package object data extends VitalsLogger {

  /**
   * a [[FabricException]] exception originating from an '''data''' aspect of fabric pipelines
   */
  final class FabricDataException(message: String, cause: Option[Throwable] = None)
    extends FabricException(message, cause)



}
