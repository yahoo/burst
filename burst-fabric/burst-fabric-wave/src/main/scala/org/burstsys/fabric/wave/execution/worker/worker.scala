/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution

import java.util.concurrent.ArrayBlockingQueue

import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.vitals.logging._

package object worker extends VitalsLogger {

  type ResultQueue = ArrayBlockingQueue[FabricGather]
}
