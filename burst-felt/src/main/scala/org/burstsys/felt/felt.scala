/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.vitals.reporter.VitalsReporter
import org.burstsys.vitals.reporter.VitalsReporterSource

/**
 * =Felt Extensible Language Toolkit=
 * <ul>
 * <li>'''model''' [[org.burstsys.felt.model]]</li>
 * </ul>
 */
package object felt extends VitalsReporterSource {

  override def reporters: Array[VitalsReporter] = Array(
    FeltReporter
  )

}
