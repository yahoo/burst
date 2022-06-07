/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test

import org.burstsys.tesla
import org.burstsys.vitals.properties.{VitalsPropertyRegistry, VitalsPropertySpecification}

package object configuration extends VitalsPropertyRegistry {

  /**
   * set up threading for master container
   */
  final def configureThreading(): Unit = {
    tesla.configuration.burstTeslaWorkerThreadCountProperty.set(Runtime.getRuntime.availableProcessors / 2)
  }

}
