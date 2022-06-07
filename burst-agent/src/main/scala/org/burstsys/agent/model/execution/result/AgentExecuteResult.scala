/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import java.io.{PrintWriter, StringWriter}
import org.burstsys.agent.api.{BurstQueryApiExecuteResult, BurstQueryApiResultStatus}
import org.burstsys.agent.api.BurstQueryApiResultStatus.BurstQueryApiExceptionStatus
import org.burstsys.vitals.errors

import scala.language.implicitConversions

object AgentExecuteResult {

  def apply(t: Throwable): AgentExecuteResult = {
    BurstQueryApiExecuteResult(
      resultStatus = BurstQueryApiExceptionStatus,
      resultMessage = errors.printStack(t),
      resultGroup = None
    )
  }

  def apply(status: BurstQueryApiResultStatus): AgentExecuteResult = {
    BurstQueryApiExecuteResult(resultStatus = status, resultMessage = status.toString, resultGroup = None)
  }

}

private final case
class AgentExecuteResultContext(_underlying_BurstQueryApiExecuteResult: BurstQueryApiExecuteResult)
  extends BurstQueryApiExecuteResult.Proxy {
}

