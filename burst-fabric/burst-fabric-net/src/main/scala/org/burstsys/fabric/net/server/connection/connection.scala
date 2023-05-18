/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server

import org.burstsys.fabric.net.message.FabricNetMsg
import org.burstsys.vitals.background.VitalsBackgroundFunctions
import org.burstsys.vitals.logging._

import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

package object connection extends VitalsLogger {

  lazy val backgroundAssessor: VitalsBackgroundFunctions =
    new VitalsBackgroundFunctions(s"fab-server-assess", 5 seconds, 10 seconds).start

  /**
   * a synchronous request/response message call used for tracking
   */
  abstract class FabricNetCall[REQUEST <: FabricNetMsg, RESPONSE <: FabricNetMsg, RESULT <: Any : ClassTag] {
    def request: REQUEST

    final var response: RESPONSE = _

    final val receipt: Promise[RESULT] = Promise[RESULT]()

    final val createTime: Long = System.currentTimeMillis()
  }

}
