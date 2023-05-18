/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.group

import org.burstsys.agent.api.BurstQueryApiGroupKey
import org.burstsys.fabric.wave.execution.model.execute
import org.burstsys.fabric.wave.execution.model.execute.group
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey

import scala.language.implicitConversions

package object key {

  type AgentGroupKey = BurstQueryApiGroupKey.Proxy

  type ThriftGroupKey = BurstQueryApiGroupKey

  private final case
  class AgentGroupKeyContext(_underlying_BurstQueryApiGroupKey: BurstQueryApiGroupKey)
    extends BurstQueryApiGroupKey.Proxy

  implicit def thriftToAgentGroupKey(a: BurstQueryApiGroupKey): AgentGroupKey = AgentGroupKeyContext(a)

  implicit def fabricToAgentGroupKey(a: FabricGroupKey): AgentGroupKey =
    BurstQueryApiGroupKey(
      groupName = a.groupName,
      groupUid = a.groupUid
    )

  implicit def thriftToFabricGroupKey(a: ThriftGroupKey): FabricGroupKey =
    group.FabricGroupKey(
      groupName = a.groupName,
      groupUid = a.groupUid
    )

}
