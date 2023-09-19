/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekClient, VitalsTrekMark, VitalsTrekRemote, VitalsTrekServer}

package object trek {

  final object SampleStoreMetadataReqTrek extends VitalsTrekMark("SampleStoreMetadataReq",
    VitalsTrekRemote, VitalsTrekClient, SpanKind.CLIENT, root = true
  )

  final object SampleStoreUpdateMetadataTrek extends VitalsTrekMark("SampleStoreUpdateMetadata",
    VitalsTrekRemote, VitalsTrekClient
  )
}
