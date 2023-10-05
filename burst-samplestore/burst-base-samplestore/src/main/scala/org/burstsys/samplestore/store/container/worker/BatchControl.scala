/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.worker

import io.opentelemetry.api.trace.Span
import org.burstsys.nexus.stream.NexusStream

class BatchControl(
                    val stream: NexusStream,
                    val feedControl: FeedControl,
                    val id: Long,
                    val itemCount: Int,
                    val maxStreamSize: Long,
                    val maxItemSize: Int,
                  )
{

  def continueProcessing: Boolean = {
    !feedControl.isCancelled && !feedControl.isSkipped
  }

  def addAttributes(span: Span): Unit = {
    feedControl.addAttributes(span)
  }

  def cancel(): Unit = {
    feedControl.cancel()
  }

  def isCancelled: Boolean = {
    feedControl.isCancelled
  }

  def processedItemsCount: Int = {
    feedControl.processedItemsCount
  }

  def expectedItemsCount: Int = {
    feedControl.expectedItemsCount
  }
  def rejectedItemsCount: Int = {
    feedControl.rejectedItemsCount
  }
  def potentialItemsCount: Int = {
    feedControl.potentialItemsCount
  }
  def skipped: Boolean = {
    feedControl.isSkipped
  }
}

