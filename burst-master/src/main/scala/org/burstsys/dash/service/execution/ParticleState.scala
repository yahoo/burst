/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.execution

import org.burstsys.dash.endpoints.ClientJsonObject
import org.burstsys.dash.service.execution.ParticleState.Update
import org.burstsys.dash.websocket.ExecutionState.{ExecutionState, unknown}
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid.VitalsUid

import scala.collection.mutable

object ParticleState {

  final case class Update(millis: Long, nanos: Long, name: String, args: Array[String])
    extends ClientJsonObject

}

final case class ParticleState(
                                ruid: VitalsUid,
                                beginMillis: Long,
                                host: String,
                                var state: ExecutionState = unknown,
                                var message: String = "",
                                var endMillis: Long = 0
                              ) extends ClientJsonObject {

  val updates: mutable.ArrayBuffer[Update] = mutable.ArrayBuffer[Update]()
}
