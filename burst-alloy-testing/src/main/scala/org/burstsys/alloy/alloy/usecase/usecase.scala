/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy

import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.vitals.logging.VitalsLogger

import java.util.concurrent.atomic.AtomicInteger
import scala.util.Random

package object usecase extends VitalsLogger {

  private val _nextHttpPort: AtomicInteger = new AtomicInteger((burstHttpPortProperty.get+ (Random.nextInt().abs % 1000)) & 0xffff)
  def getNextHttpPort: Int = _nextHttpPort.getAndIncrement() & 0xffff

}
