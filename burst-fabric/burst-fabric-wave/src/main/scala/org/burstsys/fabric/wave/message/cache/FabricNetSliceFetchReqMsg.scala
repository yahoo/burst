/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.message.cache

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.message.FabricNetSliceFetchReqMsgType
import org.burstsys.fabric.wave.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.uid._

trait FabricNetSliceFetchReqMsg extends FabricNetScatterMsg {
  def generationKey: FabricGenerationKey
}

object FabricNetSliceFetchReqMsg {
  def apply(
             guid: VitalsUid,
             ruid: VitalsUid,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             generationKey: FabricGenerationKey
           ): FabricNetSliceFetchReqMsg = {
    val msg = FabricNetSliceFetchReqContext()
    msg.guid = guid
    msg.ruid = ruid
    msg.senderKey = senderKey
    msg.receiverKey = receiverKey
    msg.generationKey = generationKey
    msg
  }

  def apply(buffer: Array[Byte]): FabricNetSliceFetchReqMsg = {
    FabricNetSliceFetchReqContext().decode(buffer)
  }

}

private final case
class FabricNetSliceFetchReqContext()
  extends FabricNetScatterMsgContext(FabricNetSliceFetchReqMsgType) with FabricNetSliceFetchReqMsg {

  var generationKey: FabricGenerationKey = _

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    generationKey = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationKey]
  }

  override def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeClassAndObject(output, generationKey)
  }
}
