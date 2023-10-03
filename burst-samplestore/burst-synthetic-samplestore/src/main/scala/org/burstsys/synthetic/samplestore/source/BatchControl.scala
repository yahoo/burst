package org.burstsys.synthetic.samplestore.source

import io.opentelemetry.api.trace.Span
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.synthetic.samplestore.trek.CANCEL_WORK_KEY

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

class BatchControl(           val stream: NexusStream,
                              val itemCount: Int,
                              val maxStreamSize: Long,
                              val maxItemSize: Int,
                              val batchCount: Int
                            ) {
  private val cancelled = new AtomicBoolean(false)
  val stats: BatchStats = BatchStats(itemCount)

  def continueProcessing: Boolean = {
    !cancelled.get() && !stats.skipped.get()
  }

  def addAttributes(span: Span): Unit = {
    stats.addAttributes(span)
    span.setAttribute(CANCEL_WORK_KEY, Boolean.box(cancelled.get()))
  }

  def cancel(): Unit = {
    cancelled.set(true)
  }

  def isCancelled: Boolean = {
    cancelled.get().booleanValue()
  }

  def processedItemsCount: Int = {
    stats.processedItemsCounter.get()
  }

  def expectedItemsCount: Int = {
    stats.expectedItemsCount.get()
  }
  def rejectedItemsCount: Int = {
    stats.rejectedItemsCounter.get()
  }
  def potentialItemsCount: Int = {
    stats.potentialItemsCounter.get()
  }
  def skipped: Boolean = {
    stats.skipped.get()
  }
}

