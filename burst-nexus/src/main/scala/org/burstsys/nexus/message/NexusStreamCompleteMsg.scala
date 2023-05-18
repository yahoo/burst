/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel._
import io.netty.buffer.ByteBuf

/**
  * sent from server to client to indicate the stream transfer has completed
  */
object NexusStreamCompleteMsg {

  def apply(stream: NexusStream, statusCode: Int): NexusStreamCompleteMsg = {
    val m = new NexusStreamCompleteMsg()
    m.link(stream)
    m.statusCode = statusCode
    m.itemCount = stream.itemCount
    m.expectedItemCount = stream.expectedItemCount
    m.potentialItemCount = stream.potentialItemCount
    m.rejectedItemCount = stream.rejectedItemCount
    m
  }

  def apply(stream: NexusStream, parcelStatus: TeslaParcelStatus): NexusStreamCompleteMsg =
    NexusStreamCompleteMsg(stream, parcelStatus.statusMarker)

  def apply(buffer: ByteBuf): NexusStreamCompleteMsg = {
    new NexusStreamCompleteMsg().decode(buffer)
  }
}

final class NexusStreamCompleteMsg() extends NexusMsg(NexusStreamCompleteMsgType) {

  var statusCode: Int = TeslaNormalStatus.statusMarker
  var itemCount: Long = 0
  var expectedItemCount: Long = 0
  var potentialItemCount: Long = 0
  var rejectedItemCount: Long = 0

  /**
    * convert a status code to a parcel status
    * @return
    */
  def status:TeslaParcelStatus = statusCode.toLong

  /**
    * convert a status code to a marker parcel
    * @return
    */
  def marker:TeslaParcel = statusCode.toLong

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def decode(buffer: ByteBuf): this.type = {
    super.decode(buffer)
    statusCode = buffer.readInt
    itemCount = buffer.readLong()
    expectedItemCount = buffer.readLong()
    potentialItemCount = buffer.readLong()
    rejectedItemCount = buffer.readLong()
    this
  }

  override
  def encode(buffer: ByteBuf): this.type = {
    super.encode(buffer)
    buffer.writeInt(statusCode)
    buffer.writeLong(itemCount)
    buffer.writeLong(expectedItemCount)
    buffer.writeLong(potentialItemCount)
    buffer.writeLong(rejectedItemCount)
    this
  }

}
