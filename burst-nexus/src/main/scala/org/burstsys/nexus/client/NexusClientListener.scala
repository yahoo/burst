/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import org.burstsys.nexus.message._

/**
 * handler for client events
 */
trait NexusClientListener extends Any {


  /**
   * Tells client side code that the stream has been initiated
   */
  def onStreamInitiated(response: NexusStreamInitiatedMsg): Unit = {}

  /**
   * Tells client side code that the another parcel in the stream has been received
   */
  def onStreamParcel(update: NexusStreamParcelMsg): Unit = {}

  /**
   * Tells client side code that the stream transfer has completed
   */
  def onStreamComplete(update: NexusStreamCompleteMsg): Unit = {}

  /**
   * Tells client side code that the server is still active
   */
  def onStreamHeartbeat(update: NexusStreamHeartbeatMsg): Unit = {}

}
