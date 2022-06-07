/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.server.connection

import java.util.concurrent.locks.ReentrantLock

import org.burstsys.nexus.NexusConnection
import org.burstsys.nexus.message.{NexusStreamInitiateMsg, msgIds}
import org.burstsys.nexus.receiver.NexusServerMsgListener
import org.burstsys.nexus.server.{NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.transmitter.NexusTransmitter
import org.burstsys.vitals.errors._
import io.netty.channel.Channel
import org.burstsys.vitals.logging._

/**
  * This is the server side representative of a [[NexusConnection]]. These are found in burst-samplestore
  */
trait NexusServerConnection extends NexusConnection with NexusServerMsgListener {

  /**
    * The data feeder for this server connection
    *
    * @return
    */
  def feeder: NexusStreamFeeder

  /**
    * optional listener for the protocol
    *
    * @param listener
    * @return
    */
  def talksTo(listener: NexusServerListener): this.type

}

object NexusServerConnection {
  def apply(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder = null): NexusServerConnection =
    NexusServerConnectionContext(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder)
}

protected final case
class NexusServerConnectionContext(channel: Channel, transmitter: NexusTransmitter, feeder: NexusStreamFeeder)
  extends AnyRef with NexusServerConnection with NexusServerParcelHandler {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _gate = new ReentrantLock()

  protected[this]
  var _inStream: Boolean = false

  protected[this]
  var _listener: NexusServerListener = _

  override
  def talksTo(listener: NexusServerListener): this.type = {
    _listener = listener
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // events
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def onStreamInitiateMsg(request: NexusStreamInitiateMsg): Unit = {
    _gate.lock()
    try {
      initiateStream(request, request.guid, request.suid)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    } finally _gate.unlock()
  }

}
