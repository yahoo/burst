/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.message.wave

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.message.FabricNetRespMsg
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.wave.message.FabricNetParticleRespMsgType
import org.burstsys.fabric.topology.model.node.FabricNode

trait FabricNetParticleRespMsg extends FabricNetScatterMsg with FabricNetRespMsg[FabricGather]

/**
 * sent from client to server to acknowledge completion and optionally results of a wave scan
 */
object FabricNetParticleRespMsg {

  def apply(request: FabricNetParticleReqMsg, senderKey: FabricNode, receiverKey: FabricNode,
            gather: FabricGather): FabricNetParticleRespMsg = {
    val m = FabricNetParticleRespMsgContext()
    link(request, senderKey, receiverKey, m)
    m.success(gather)
  }

  def apply(request: FabricNetParticleReqMsg, senderKey: FabricNode, receiverKey: FabricNode,
            exception: Throwable): FabricNetParticleRespMsg = {
    val m = FabricNetParticleRespMsgContext()
    link(request, senderKey, receiverKey, m)
    m.failure(exception)
  }

  private def link(request: FabricNetParticleReqMsg, senderKey: FabricNode, receiverKey: FabricNode, m: FabricNetParticleRespMsgContext): Unit = {
    m.link(request)
    m.senderKey = senderKey
    m.receiverKey = receiverKey
  }

  def apply(buffer: Array[Byte]): FabricNetParticleRespMsg = {
    FabricNetParticleRespMsgContext().decode(buffer)
  }

}

final case
class FabricNetParticleRespMsgContext()
  extends FabricNetScatterMsgContext(FabricNetParticleRespMsgType) with FabricNetParticleRespMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    readResponse(kryo, input)
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    writeResponse(kryo, output)
  }

}
