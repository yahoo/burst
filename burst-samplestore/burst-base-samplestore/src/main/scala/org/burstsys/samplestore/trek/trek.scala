/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekClient, VitalsTrekMark, VitalsTrekRemote, VitalsTrekServer, VitalsTrekWorker}

package object trek {

  final object SampleStoreMetadataReqTrek extends VitalsTrekMark("SampleStoreMetadataReq",
    VitalsTrekRemote, VitalsTrekClient, SpanKind.CLIENT, root = true
  )

  final object SampleStoreUpdateMetadataTrek extends VitalsTrekMark("SampleStoreUpdateMetadata",
    VitalsTrekRemote, VitalsTrekClient
  )

  private[samplestore] val REJECTED_ITEMS_KEY = AttributeKey.longKey("burstsys.rejectedItems")
  private[samplestore] val PROCESSED_ITEMS_COUNT_KEY = AttributeKey.longKey("burstsys.processedItems")
  private[samplestore] val CANCEL_WORK_KEY = AttributeKey.booleanKey("burstsys.cancelWork")
  private[samplestore] val SKIPPED_KEY = AttributeKey.booleanKey("burstsys.skipped")
  private[samplestore] val EXPECTED_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.expectedItems")
  private[samplestore] val POTENTIAL_ITEM_COUNT_KEY = AttributeKey.longKey("burstsys.potentialItems")

  final object ScanningFeedStreamTrek extends VitalsTrekMark("ScanningFeedStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )

  final object ScanningBatchTrek extends VitalsTrekMark("ScanningBatch",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.INTERNAL
  )
}
