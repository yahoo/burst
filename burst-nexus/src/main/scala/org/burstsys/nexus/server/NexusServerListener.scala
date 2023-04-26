/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server

import org.burstsys.nexus.message.{NexusStreamAbortMsg, NexusStreamInitiateMsg}
import org.burstsys.nexus.server.connection.NexusServerConnection
import org.burstsys.nexus.stream.NexusStream

/**
  * handler for server events
  */
trait NexusServerListener extends Any {

  /**
    * Tells server side code that the client has requested a stream be initiated
    */
  def onStreamInitiate(stream: NexusStream, request: NexusStreamInitiateMsg): Unit = {}

  /**
    * client asked for stream to be aborted and cleaned up
    */
  def onStreamAbort(stream: NexusStream, request: NexusStreamAbortMsg):Unit = {}

  def onClientDisconnect(connection: NexusServerConnection): Unit = {}
}
