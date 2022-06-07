/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.cache

import org.burstsys.agent.api.BurstQueryApiResultStatus.{BurstQueryApiExceptionStatus, BurstQueryApiSuccessStatus}
import org.burstsys.agent.api.{BurstQueryApiResultStatus, BurstQueryCacheGeneration, BurstQueryCacheGenerationResult}

import scala.language.implicitConversions


object AgentGenerationResult {

  def apply(
             status: BurstQueryApiResultStatus = BurstQueryApiSuccessStatus,
             message: String = "ok",
             generations: Option[Seq[BurstQueryCacheGeneration]] = None
           ): BurstQueryCacheGenerationResult = BurstQueryCacheGenerationResult(status, message, generations)

  def apply(error: String): BurstQueryCacheGenerationResult =
    AgentGenerationResult(status = BurstQueryApiExceptionStatus, message = error)

  @scala.annotation.tailrec
  def apply(generations: Seq[BurstQueryCacheGeneration]): BurstQueryCacheGenerationResult =
    AgentGenerationResult(generations = generations)

}
