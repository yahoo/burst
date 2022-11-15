/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.api

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.agent.configuration.burstAgentApiMaxConcurrencyProperty
import org.burstsys.vitals.logging._

package object server extends VitalsLogger {

  // lazy so that when we're reading config from the db we have a chance to set the system properties
  lazy val maxConcurrency: Int = burstAgentApiMaxConcurrencyProperty.get

  final val maxConcurrencyGate = new AtomicInteger()

}
