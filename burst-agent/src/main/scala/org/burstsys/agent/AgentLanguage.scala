/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.metadata.model.over.FabricOver

import scala.concurrent.Future

trait AgentLanguage extends Any {

  def languagePrefixes: Array[String]

  /**
   * Execute a language group pipeline via the fabric protocol.
   *
   * @param groupUid
   * @param source
   * @param over
   * @param call
   * @return
   */
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall] = None): Future[FabricExecuteResult]
}
