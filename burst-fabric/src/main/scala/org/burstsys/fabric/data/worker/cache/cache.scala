/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.worker

import org.burstsys.vitals.logging._

import scala.language.{implicitConversions, postfixOps}

package object cache extends VitalsLogger {

  /**
   * singleton cache for use by all worker containers in this JVM context (currently the only
   * scenario where you might want more than one is a unit test)
   */
  final val instance: FabricSnapCache = FabricSnapCache()

}
