/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel

import org.burstsys.vitals.logging._

package object pipe extends VitalsLogger {

  val logOnNonStatus: Boolean = log.isTraceEnabled
  val queueLogInterval: Long = 10

  def logNonStatusParcel(count: Long): Boolean = logOnNonStatus && (count % queueLogInterval == 0)

}
