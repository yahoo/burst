/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekMark, VitalsTrekRemote, VitalsTrekWorker}

package object trek {
  final object SampleSourceFeedStreamTrek extends VitalsTrekMark("FeedStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )

  private[samplesource] val REJECTED_ITEMS_KEY = AttributeKey.longKey("burstsys.rejectedItems")
  private[samplesource] val PROCESSED_ITEMS_COUNT_KEY = AttributeKey.longKey("burstsys.processedItems")
  private[samplesource] val CANCEL_WORK_KEY = AttributeKey.booleanKey("burstsys.cancelWork")
  private[samplesource] val SKIPPED_KEY = AttributeKey.booleanKey("burstsys.skipped")
  private[samplesource] val EXPECTED_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.expectedItems")
  private[samplesource] val POTENTIAL_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.potentialItems")

  final object ScanningFeedStreamTrek extends VitalsTrekMark("ScanningFeedStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )

  final object ScanningBatchTrek extends VitalsTrekMark("ScanningBatch",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.INTERNAL
  )
}
