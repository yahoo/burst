/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.cache

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.net.message.FabricNetCacheOperationRespMsgType
import org.burstsys.fabric.net.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import io.netty.buffer.ByteBuf

trait FabricNetCacheOperationRespMsg extends FabricNetScatterMsg {

  /**
    * the generations that match the parameters sent in the request
    */
  def generations: Array[FabricGeneration]

}

object FabricNetCacheOperationRespMsg {

  def apply(req: FabricNetCacheOperationReqMsg, senderKey: FabricNode, receiverKey: FabricNode,
            generations: Array[FabricGeneration] = Array()): FabricNetCacheOperationRespMsg = {
    val msg = FabricNetCacheOperationRespMsgContext()
    msg.link(req)
    msg.senderKey = senderKey
    msg.receiverKey = receiverKey
    msg.generations = generations
    msg
  }

  def apply(buffer: ByteBuf): FabricNetCacheOperationRespMsg = {
    FabricNetCacheOperationRespMsgContext().decode(buffer)
  }

}

private final case
class FabricNetCacheOperationRespMsgContext()
  extends FabricNetScatterMsgContext(FabricNetCacheOperationRespMsgType) with FabricNetCacheOperationRespMsg {

  var generations: Array[FabricGeneration] = _

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    val len = input.readLong(true).toInt
    generations = (0 until len).map(_ => kryo.readClassAndObject(input).asInstanceOf[FabricGeneration]).toArray
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(generations.length, true)
    generations.foreach { g => kryo.writeClassAndObject(output, g) }
  }

}
