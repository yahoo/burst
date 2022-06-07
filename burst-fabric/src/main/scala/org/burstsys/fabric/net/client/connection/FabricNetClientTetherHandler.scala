/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client.connection

import org.burstsys.fabric.net.message.assess.FabricNetTetherMsg
import org.burstsys.fabric.net.newRequestId
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.git

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * A background tether heartbeat
 */
trait FabricNetClientTetherHandler extends AnyRef {

  self: FabricNetClientConnection =>

  lazy val tetherLineFunction = new VitalsBackgroundFunction(
    "fab-client-tether",
    100 milliseconds, 10 seconds, {
      val c = channel
      if (c != null && c.isActive) {
        val message = FabricNetTetherMsg(newRequestId, clientKey, serverKey, git.commitId)
        transmitter.transmitControlMessage(message)
      }
    }
  )

}
