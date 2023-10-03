package org.burstsys.synthetic.samplestore.source

import io.opentelemetry.api.trace.Span
import org.burstsys.synthetic.samplestore.trek._

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import scala.concurrent.duration.Duration

class FeedControl(val timeout: Duration, itemCount: Int) {
  private val processedItemsCounter = new AtomicInteger()
  private val rejectedItemsCounter = new AtomicInteger()
  private val potentialItemsCounter = new AtomicInteger(itemCount)
  private val expectedItemsCounter = new AtomicInteger(itemCount)
  private val skipped = new AtomicBoolean(false)
  private val cancelled = new AtomicBoolean(false)

  def addAttributes(span: Span): Unit = {
    span.setAttribute(REJECTED_ITEMS_KEY, rejectedItemsCounter.get())
    span.setAttribute(PROCESSED_ITEMS_COUNT_KEY, processedItemsCounter.get())
    span.setAttribute(EXPECTED_ITEM_COUNT_KEY, expectedItemsCounter.get())
    span.setAttribute(POTENTIAL_ITEM_COUNT_KEY, potentialItemsCounter.get())
    span.setAttribute(SKIPPED_KEY, Boolean.box(skipped.get()))
    span.setAttribute(CANCEL_WORK_KEY, Boolean.box(cancelled.get()))
  }

  def continueProcessing: Boolean = {
    !cancelled.get() && !skipped.get()
  }

  def cancel(): Unit = {
    cancelled.set(true)
  }

  def isCancelled: Boolean = {
    cancelled.get().booleanValue()
  }

  def processedItemsCount: Int = {
    processedItemsCounter.get()
  }

  def expectedItemsCount: Int = {
    expectedItemsCounter.get()
  }

  def rejectedItemsCount: Int = {
    rejectedItemsCounter.get()
  }

  def potentialItemsCount: Int = {
    potentialItemsCounter.get()
  }

  def isSkipped: Boolean = {
    skipped.get()
  }
}
