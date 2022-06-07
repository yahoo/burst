/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.cache

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.net.message.FabricNetSliceFetchRespMsgType
import org.burstsys.fabric.net.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import io.netty.buffer.ByteBuf

trait FabricNetSliceFetchRespMsg extends FabricNetScatterMsg {
  def slices: Array[FabricSliceMetadata]
}

object FabricNetSliceFetchRespMsg {
  def apply(
             req: FabricNetSliceFetchReqMsg,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             slices: Array[FabricSliceMetadata] = Array()
           ): FabricNetSliceFetchRespMsg = {
    val msg = FabricNetSliceFetchRespContext()
    msg.link(req)
    msg.senderKey = senderKey
    msg.receiverKey = receiverKey
    msg.slices = slices
    msg
  }

  def apply(buffer: ByteBuf): FabricNetSliceFetchRespMsg = {
    FabricNetSliceFetchRespContext().decode(buffer)
  }

}

private final case
class FabricNetSliceFetchRespContext()
  extends FabricNetScatterMsgContext(FabricNetSliceFetchRespMsgType) with FabricNetSliceFetchRespMsg {

  var slices: Array[FabricSliceMetadata] = _

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    val count = input.readLong(true).toInt
    slices = (0 until count).map { _ => kryo.readClassAndObject(input).asInstanceOf[FabricSliceMetadata] }.toArray
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(slices.length, true)
    slices.foreach { s => kryo.writeClassAndObject(output, s) }
  }
}
