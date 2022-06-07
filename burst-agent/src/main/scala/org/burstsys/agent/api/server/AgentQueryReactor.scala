/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api.server

import java.util.concurrent.TimeUnit

import org.burstsys.agent.api.{AgentApi, BurstQueryApiCall}
import org.burstsys.agent.model.execution.group.call._
import org.burstsys.agent.model.execution.group.over._
import org.burstsys.agent.model.execution.result.{ThriftAgentExecuteResult, _}
import org.burstsys.api._
import org.burstsys.fabric.execution.model.execute.group.{FabricGroupUid, sanitizeGuid}
import org.burstsys.tesla.thread.request._
import com.twitter.util.{Future => TwitterFuture}

trait AgentQueryReactor extends AgentApi {

  self: AgentApiServer =>

  final override def groupExecute(groupUid: Option[FabricGroupUid], source: String, over: AgentThriftOver, call: Option[BurstQueryApiCall]): TwitterFuture[ThriftAgentExecuteResult] = {
    val start = System.nanoTime
    TeslaRequestFuture {
      ensureRunning
      sanitizeGuid(groupUid)
    } chainWithFuture { guid =>
      if (!groupUid.exists(g => guid.startsWith(g))) {
        log info s"AGENT_THRIFT_GUID_INVALID provided='${groupUid.orNull}' guid='$guid'"
      }
      service.execute(source, over, guid, call.map(thriftToFabricCall)) map fabricToAgentExecuteResult
    } andThen {
      case _ =>
        log info s"THRIFT_API agent request complete duration=${TimeUnit.NANOSECONDS.toMillis(System.nanoTime - start)}"
    }
  }
}
