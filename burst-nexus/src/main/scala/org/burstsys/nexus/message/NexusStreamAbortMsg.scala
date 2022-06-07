/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel._
import io.netty.buffer.ByteBuf

/**
  * sent from server to client to provide an array of data items (for batch mode transfers)
  */
object NexusStreamAbortMsg {

  def apply(stream: NexusStream, status: TeslaParcelStatus): NexusStreamAbortMsg = {
    val m = new NexusStreamAbortMsg()
    m.link(stream)
    m
  }

  def apply(buffer: ByteBuf): NexusStreamAbortMsg = {
    new NexusStreamAbortMsg().decode(buffer)
  }
}

final
class NexusStreamAbortMsg() extends NexusMsg(NexusStreamAbortMsgType) {

  var status: TeslaParcelStatus = _

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def decode(buffer: ByteBuf): this.type = {
    super.decode(buffer)
    status = buffer.readLong
    this
  }

  override
  def encode(buffer: ByteBuf): this.type = {
    super.encode(buffer)
    buffer.writeLong(status.statusMarker)
    this
  }

}
