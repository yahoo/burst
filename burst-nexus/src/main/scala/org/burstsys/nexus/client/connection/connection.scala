/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.language.postfixOps

package object connection extends VitalsLogger {

  private[connection] final val reportPeriodMs = (2 seconds).toMillis

  private[connection] final val initiateTimeoutDuration = 25 seconds

  private[connection] final val initiateMaxWaits = 50

  private[connection] final val initiateWaitPeriodMs = initiateTimeoutDuration.toMillis / initiateMaxWaits


}
