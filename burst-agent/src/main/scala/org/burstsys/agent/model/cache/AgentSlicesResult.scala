/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache

import org.burstsys.agent.api.BurstQueryApiResultStatus.{BurstQueryApiExceptionStatus, BurstQueryApiSuccessStatus}
import org.burstsys.agent.api.{BurstQueryApiResultStatus, BurstQueryCacheSlice, BurstQuerySliceGenerationResult}

import scala.language.implicitConversions


object AgentSlicesResult {

  def apply(
             status: BurstQueryApiResultStatus = BurstQueryApiSuccessStatus,
             message: String = "ok",
             slices: Option[Seq[BurstQueryCacheSlice]] = None
           ): BurstQuerySliceGenerationResult =
    BurstQuerySliceGenerationResult(status, message, slices)

  def apply(error: String): BurstQuerySliceGenerationResult =
    AgentSlicesResult(status = BurstQueryApiExceptionStatus, message = error)

  @scala.annotation.tailrec
  def apply(slices: Seq[BurstQueryCacheSlice]): BurstQuerySliceGenerationResult =
    AgentSlicesResult(slices = slices)


}
