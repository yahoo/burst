/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.logging._

package object threading extends VitalsLogger {

  final implicit
  val burstThreadGroupGlobal: ThreadGroup = new ThreadGroup("Burst")

}
