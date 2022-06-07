/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel._
import io.netty.buffer.ByteBuf

/**
  * sent from server to client to indicate the server is still active
  */
object NexusStreamHeartbeatMsg {

  def apply(stream: NexusStream): NexusStreamHeartbeatMsg = {
    val m = new NexusStreamHeartbeatMsg()
    m.link(stream)
    m
  }

  def apply(buffer: ByteBuf): NexusStreamHeartbeatMsg = {
    new NexusStreamHeartbeatMsg().decode(buffer)
  }
}

final
class NexusStreamHeartbeatMsg()
  extends NexusMsg(NexusStreamHeartbeatMsgType) {

  var statusCode: Int = TeslaHeartbeatStatus.statusMarker

  /**
    * convert a status code to a marker parcel
    * @return
    */
  def marker: TeslaParcel = statusCode.toLong

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def decode(buffer: ByteBuf): this.type = {
    super.decode(buffer)
    statusCode = buffer.readInt
    this
  }

  override
  def encode(buffer: ByteBuf): this.type = {
    super.encode(buffer)
    buffer.writeInt(statusCode)
    this
  }

}
