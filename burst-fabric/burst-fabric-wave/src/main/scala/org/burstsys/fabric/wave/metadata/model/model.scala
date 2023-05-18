/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata

import org.burstsys.vitals.logging.VitalsLogger

package object model extends VitalsLogger {

  /**
   * Unique Global Key For Domains
   */
  type FabricDomainKey = Long

  /**
   * Unique Global Key For Views
   */
  type FabricViewKey = Long

  /**
   * Unique __within the View__ Key For View Generations
   */
  type FabricGenerationClock = Long

  /**
   * this is the marker for a 'fake' generation key
   */
  final val FabricLatestGenerationClock = 0L

}
