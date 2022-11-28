/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store

import org.burstsys.vitals.logging.VitalsLogger

package object container extends VitalsLogger {
  val NexusPortAssessParameterName = "burstsys.nexus.port"
  val NexusConnectedPortAssessParameterName = "burstsys.nexus.connectedPort"
  val NexusHostNameAssessParameterName = "burstsys.nexus.hostname"
  val NexusHostAddrAssessParameterName = "burstsys.nexus.hostaddr"
}
