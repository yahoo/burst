/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekMark, VitalsTrekRemote, VitalsTrekWorker}

package object trek {
  private[samplestore] val BATCH_ID_KEY = AttributeKey.longKey("burstsys.batchId")
  private[samplestore] val REJECTED_ITEMS_KEY = AttributeKey.longKey("burstsys.rejectedItems")
  private[samplestore] val PROCESSED_ITEMS_COUNT_KEY = AttributeKey.longKey("burstsys.processedItems")
  private[samplestore] val CANCEL_WORK_KEY = AttributeKey.booleanKey("burstsys.cancelWork")
  private[samplestore] val SKIPPED_KEY = AttributeKey.booleanKey("burstsys.skipped")
  private[samplestore] val EXPECTED_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.expectedItems")
  private[samplestore] val POTENTIAL_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.potentialItems")

  final object SyntheticFeedStreamTrek extends VitalsTrekMark("SyntheticFeedStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )

  final object SyntheticBatchTrek extends VitalsTrekMark("SyntheticBatch",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.INTERNAL
  )
}
