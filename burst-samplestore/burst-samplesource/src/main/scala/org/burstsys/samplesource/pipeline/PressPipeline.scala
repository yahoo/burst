/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.pipeline

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.nexus.stream.NexusStream

import scala.concurrent.Future

trait PressPipeline {
  def pressToFuture(stream: NexusStream, pressSource: BrioPressSource, schema: BrioSchema, version: BrioVersionKey,
                    maxItemSize: Int, maxTotalBytes: Long): Future[PressJobResults]

}
