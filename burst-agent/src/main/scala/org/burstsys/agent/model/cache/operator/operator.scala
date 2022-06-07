/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache

import org.burstsys.agent.api.BurstQueryCacheOperation.{Evict, Flush, Search}
import org.burstsys.agent.api.BurstQueryOperator.{Eq, Gt, Lt}
import org.burstsys.agent.api.{BurstQueryCacheOperation, BurstQueryOperator}
import org.burstsys.fabric.data.model.ops._

import scala.language.implicitConversions

package object operator {

  implicit def thriftToFabricCacheOperation(operation: BurstQueryCacheOperation): FabricCacheManageOp = operation match {
    case Search => FabricCacheSearch
    case Evict => FabricCacheEvict
    case Flush => FabricCacheFlush
    case _ => ???
  }

  implicit def fabricToThriftCacheOperation(operation: FabricCacheManageOp): BurstQueryCacheOperation = operation match {
    case FabricCacheSearch => Search
    case FabricCacheEvict => Evict
    case FabricCacheFlush => Flush
    case _ => ???
  }

  implicit def thriftToFabricCacheOperator(o: BurstQueryOperator): FabricCacheOpRelation = o match {
    case Lt => FabricCacheLT
    case Gt => FabricCacheGT
    case Eq => FabricCacheEQ
    case _ => ???
  }

  implicit def fabricCacheOperatorToThrift(o: FabricCacheOpRelation): BurstQueryOperator = o match {
    case FabricCacheLT => Lt
    case FabricCacheGT => Gt
    case FabricCacheEQ => Eq
    case _ => ???
  }
}
