/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.{VitalsPropertyKey, VitalsPropertyValue}

package object service extends VitalsLogger {
  type MetadataParameters = scala.collection.Map[VitalsPropertyKey, java.io.Serializable]
}
