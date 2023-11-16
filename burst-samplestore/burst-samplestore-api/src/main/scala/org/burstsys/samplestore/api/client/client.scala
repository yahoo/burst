/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.samplestore.api
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostName, VitalsHostPort}

package object client extends VitalsLogger{
  final def defaultHostName: VitalsHostName = api.configuration.burstSampleStoreApiHostProperty.get

  final def defaultPort: VitalsHostPort = api.configuration.burstSampleStoreApiPortProperty.get
}
