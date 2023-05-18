/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.topology.model.node.worker.FabricWorkerROProxy
import org.burstsys.vitals.logging._

import scala.language.implicitConversions

package object topology extends VitalsLogger {
  type FabricTopologyWorker = FabricWorkerROProxy

}
