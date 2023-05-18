/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

package object source {

  /** `synthetic-samplesource` */
  val TestSampleSourceName = "test-base-samplesource"

  /** `consistent-hash` */
  val InvariantHash = "consistent-hash"

  var testListener: Option[TestSourceListener] = None

}
