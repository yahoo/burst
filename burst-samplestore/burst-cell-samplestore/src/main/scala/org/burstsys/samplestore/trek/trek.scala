/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.vitals.trek.{VitalsTrekCell, VitalsTrekMark, VitalsTrekWorker}

package object trek {

  final object SampleStoreLoadTrekMark extends VitalsTrekMark("sample_store_load",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderInitializeTrekMark extends VitalsTrekMark("sample_store_loader_init",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderOpenTrekMark extends VitalsTrekMark("sample_store_loader_open",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderAcquireTrekMark extends VitalsTrekMark("sample_store_loader_acquire",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderProcessStreamTrekMark extends VitalsTrekMark("sample_store_loader_process_stream",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderWaitForWritesTrekMark extends VitalsTrekMark("sample_store_loader_wait_for_writes",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderReleaseStreamsTrekMark extends VitalsTrekMark("sample_store_loader_release_streams",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderCloseWritesTrekMark extends VitalsTrekMark("sample_store_loader_close_writes",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )

  final object SampleStoreLoaderProcessCompletionTrekMark extends VitalsTrekMark("sample_store_loader_process_completion",
    cluster = VitalsTrekCell,
    role = VitalsTrekWorker
  )
}
