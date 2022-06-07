/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes.{TeslaMemorySize, _}
import org.burstsys.tesla.parcel.{TeslaParcel, _}
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import io.netty.buffer.ByteBuf

/**
 * sent from server to client to provide an array of data items (for batch mode transfers)
 */
object NexusStreamParcelMsg {

  def apply(stream: NexusStream, parcel: TeslaParcel): NexusStreamParcelMsg = {
    val m = new NexusStreamParcelMsg()
    m.link(stream)
    m.parcel = parcel
    m
  }

  def apply(buffer: ByteBuf): NexusStreamParcelMsg = {
    new NexusStreamParcelMsg().decode(buffer)
  }
}


/**
 * sent from server to client to provide a single data item (for non batch mode transfers)
 */
final
class NexusStreamParcelMsg() extends NexusMsg(NexusStreamParcelMsgType) {

  var parcel: TeslaParcel = TeslaEndMarkerParcel

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * Decode the contents of a Netty ByteBuf into a parcel.
   * Decode copies the deflated parcel
   *
   * @param buffer source netty buffer
   * @return uninflated parcel
   */
  override
  def decode(buffer: ByteBuf): this.type = {
    TeslaWorkerCoupler {
      super.decode(buffer)
      // read total length
      val length: TeslaMemorySize = buffer.readInt
      // read the number of buffers
      val number = buffer.readInt()
      // read the inflated size
      val inflatedSize: TeslaMemorySize = buffer.readInt()
      parcel = tesla.parcel.factory.grabParcel(length)
      // transfer
      val ptr: TeslaMemoryPtr = buffer.memoryAddress() + buffer.readerIndex()
      parcel.fromDeflatedMemoryPtr(number, inflatedSize, length, ptr)
      this
    }
  }

  /**
   * Encode the parcel into the Netty ByteBuf
   * It deflates the buffers directly
   *
   * @return
   */
  override
  def encode(buffer: ByteBuf): this.type = {
    lazy val hdr = s"NexusStreamParcelMsg.encode"
    TeslaWorkerCoupler {
      super.encode(buffer)
      val lengthWIndex = buffer.writerIndex
      // write length
      buffer.writeInt(0)
      // write number of buffers
      buffer.writeInt(parcel.bufferCount)
      // write inflated size
      buffer.writeInt(parcel.currentUsedMemory)

      // deflate directly into the buffer
      val ptr: TeslaMemoryPtr = buffer.memoryAddress()
      val deflatedSize: Int = parcel.deflateTo(ptr + buffer.writerIndex).toInt
      buffer.setInt(lengthWIndex, deflatedSize)
      buffer.writerIndex(buffer.writerIndex + deflatedSize)
      this
    }
  }

}
