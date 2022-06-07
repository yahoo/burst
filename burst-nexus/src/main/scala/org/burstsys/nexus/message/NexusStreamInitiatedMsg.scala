/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.NexusStreamUid
import io.netty.buffer.ByteBuf

/**
  * sent from server to client to acknowledge the creation of a new stream
  */
object NexusStreamInitiatedMsg {

  def apply(request: NexusStreamInitiateMsg, suid: NexusStreamUid): NexusStreamInitiatedMsg = {
    val m = new NexusStreamInitiatedMsg()
    m.link(request)
    m
  }

  def apply(buffer: ByteBuf): NexusStreamInitiatedMsg = {
    new NexusStreamInitiatedMsg().decode(buffer)
  }
}

final
class NexusStreamInitiatedMsg()
  extends NexusMsg(NexusStreamInitiatedMsgType) {

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def decode(buffer: ByteBuf): this.type = {
    super.decode(buffer)
    this
  }

  override
  def encode(buffer: ByteBuf): this.type = {
    super.encode(buffer)
    this
  }

}
