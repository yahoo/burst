/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.vitals.logging._

/**
 * Results transformation
 */
package object transform extends VitalsLogger {

  trait AgentTransform extends Function[FabricExecuteResult, FabricExecuteResult]

  final object Identity extends AgentTransform {
    override def apply(results: FabricExecuteResult): FabricExecuteResult = results
  }

}
