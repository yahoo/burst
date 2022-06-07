/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import org.burstsys.nexus.message.{NexusStreamAbortMsg, NexusStreamCompleteMsg, NexusStreamHeartbeatMsg, NexusStreamInitiateMsg, NexusStreamInitiatedMsg, NexusStreamParcelMsg}
import org.burstsys.vitals.logging._

package object receiver extends VitalsLogger {
  /**
   * nexus client msg event handler
   */
  trait NexusClientMsgListener extends Any {

    def onStreamParcelMsg(msg: NexusStreamParcelMsg): Unit

    def onStreamInitiatedMsg(msg: NexusStreamInitiatedMsg): Unit

    def onStreamCompleteMsg(msg: NexusStreamCompleteMsg): Unit

    def onStreamHeartbeatMsg(msg: NexusStreamHeartbeatMsg): Unit

  }

  /**
   * nexus server msg event handler
   */
  trait NexusServerMsgListener extends Any {

    def onStreamAbortMsg(msg: NexusStreamAbortMsg): Unit

    def onStreamInitiateMsg(msg: NexusStreamInitiateMsg): Unit

  }

}
