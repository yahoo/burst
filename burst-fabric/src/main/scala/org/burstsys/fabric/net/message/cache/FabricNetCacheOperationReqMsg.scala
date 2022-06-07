/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.cache

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops.FabricCacheManageOp
import org.burstsys.fabric.net.message.FabricNetCacheOperationReqMsgType
import org.burstsys.fabric.net.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.uid._
import io.netty.buffer.ByteBuf

trait FabricNetCacheOperationReqMsg extends FabricNetScatterMsg {

  /**
   * The operation to perform
   */
  def operation: FabricCacheManageOp

  /**
   * The generation spec to operate over.
   *
   * Fields can be either specified or `-1`, any field containing `-1`
   * will be treated as a wildcard, matching all values. Fields must
   * be specified in order for least general to most general.
   * i.e. specifying generationClock means that both the domain _and_ view
   * are also specified.
   */
  def generationKey: FabricGenerationKey
}

object FabricNetCacheOperationReqMsg {

  def apply(
             guid: VitalsUid,
             ruid: VitalsUid,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             operation: FabricCacheManageOp,
             generationKey: FabricGenerationKey
           ): FabricNetCacheOperationReqMsg = {
    val msg = FabricNetCacheOperationReqMsgContext()
    msg.guid = guid
    msg.ruid = ruid
    msg.senderKey = senderKey
    msg.receiverKey = receiverKey
    msg.operation = operation
    msg.generationKey = generationKey
    msg
  }

  def apply(buffer: ByteBuf): FabricNetCacheOperationReqMsg = {
    FabricNetCacheOperationReqMsgContext().decode(buffer)
  }
}

private final case
class FabricNetCacheOperationReqMsgContext()
  extends FabricNetScatterMsgContext(FabricNetCacheOperationReqMsgType) with FabricNetCacheOperationReqMsg {

  var operation: FabricCacheManageOp = _
  var generationKey: FabricGenerationKey = _

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    operation = kryo.readClassAndObject(input).asInstanceOf[FabricCacheManageOp]
    generationKey = kryo.readClassAndObject(input).asInstanceOf[FabricGenerationKey]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeClassAndObject(output, operation)
    kryo.writeClassAndObject(output, generationKey)
  }
}
