/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import io.opentelemetry.api.trace.SpanKind
import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekSupervisor, VitalsTrekWorker}

package object trek {

  final object SampleStoreGetViewGeneratorTrek extends VitalsTrekMark("getViewGenerator",
    VitalsTrekCell, VitalsTrekSupervisor, SpanKind.CLIENT
  )

  final object SampleStoreLoadTrekMark extends VitalsTrekMark("SampleStoreLoad",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object SampleStoreLoaderProcessStreamTrekMark extends VitalsTrekMark("SampleStoreLoaderProcessStream",
    VitalsTrekCell, VitalsTrekWorker
  )

  final object SampleStoreLoaderReleaseStreamsTrekMark extends VitalsTrekMark("SampleStoreLoaderReleaseStreams",
    VitalsTrekCell, VitalsTrekWorker
  )

}
