/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store

import org.burstsys.vitals.logging.VitalsLogger

package object container extends VitalsLogger {
  val NexusPortAccessParameter = "burstsys.nexus.port"
  val NexusConnectedPortAccessParameter = "burstsys.nexus.connectedPort"
  val NexusHostNameAccessParameter = "burstsys.nexus.hostname"
  val NexusHostAddrAccessParameter = "burstsys.nexus.hostAddress"
}
