/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.vitals.git.VitalsGitReporter
import org.burstsys.vitals.host.VitalsDescriptorReporter
import org.burstsys.vitals.host.VitalsGcReporter
import org.burstsys.vitals.host.VitalsHostReporter
import org.burstsys.vitals.host.VitalsMemoryReporter
import org.burstsys.vitals.host.VitalsNativeMemoryReporter
import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource
import org.burstsys.vitals.threading.VitalsThreadReporter

package object vitals extends VitalsReporterSource {

  // TODO this will need to be carefully used as we go open source and support providers
  final val burstPackage = "org.burstsys"

  override def reporters: Array[VitalsReporter] = Array(
    VitalsGitReporter,
    VitalsHostReporter,
    VitalsDescriptorReporter,
    VitalsMemoryReporter,
    VitalsNativeMemoryReporter,
    VitalsGcReporter,
    VitalsThreadReporter,
  )

}
