/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.assess

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.message._
import org.burstsys.fabric.net.FabricNetMessageId
import org.burstsys.fabric.topology.model.node.FabricNode
import io.netty.buffer.ByteBuf

trait FabricNetAssessReqMsg extends FabricNetMsg {

  /**
    * TODO
    *
    * @return
    */
  def sentNanos: Long
}

/**
  * sent from server to client to get a current assessment
  */
object FabricNetAssessReqMsg {

  def apply(ruid: FabricNetMessageId, senderKey: FabricNode, receiverKey: FabricNode): FabricNetAssessReqMsg = {
    val m = FabricNetAssessReqMsgContext()
    m.messageId = ruid
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.sentNanos = System.nanoTime
    m
  }

  def apply(buffer: ByteBuf): FabricNetAssessReqMsg = {
    FabricNetAssessReqMsgContext().decode(buffer)
  }

}

private final case
class FabricNetAssessReqMsgContext()
  extends FabricNetMsgContext(FabricNetAssessReqMsgType) with FabricNetAssessReqMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  var sentNanos: Long = _

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    sentNanos = input.readLong()
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(sentNanos)
  }

}
