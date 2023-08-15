/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekWorker}

package object trek {

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
