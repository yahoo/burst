package org.burstsys.synthetic.samplestore.source

import io.opentelemetry.api.trace.Span
import org.burstsys.synthetic.samplestore.trek.{CANCEL_WORK_KEY, EXPECTED_ITEM_COUNT_KEY, PROCESSED_ITEMS_COUNT_KEY, POTENTIAL_ITEM_COUNT_KEY, REJECTED_ITEMS_KEY, SKIPPED_KEY}

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

case class BatchStats(val itemCount: Int) {
  val processedItemsCounter = new AtomicInteger()
  val rejectedItemsCounter = new AtomicInteger()
  val potentialItemsCounter = new AtomicInteger(itemCount)
  val expectedItemsCount = new AtomicInteger(itemCount)
  val skipped = new AtomicBoolean(false)

  def addAttributes(span: Span): Unit = {
    span.setAttribute(REJECTED_ITEMS_KEY, rejectedItemsCounter.get())
    span.setAttribute(PROCESSED_ITEMS_COUNT_KEY, processedItemsCounter.get())
    span.setAttribute(EXPECTED_ITEM_COUNT_KEY, expectedItemsCount.get())
    span.setAttribute(POTENTIAL_ITEM_COUNT_KEY, potentialItemsCounter.get())
    span.setAttribute(SKIPPED_KEY, Boolean.box(skipped.get()))
  }
}
