/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekMark, VitalsTrekRemote, VitalsTrekWorker}

package object trek {

  final object SampleSourceFeedStreamTrek extends VitalsTrekMark("FeedStream",
    VitalsTrekRemote, VitalsTrekWorker, SpanKind.PRODUCER
  )
}
