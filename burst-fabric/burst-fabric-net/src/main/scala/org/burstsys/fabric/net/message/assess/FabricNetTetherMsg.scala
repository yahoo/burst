/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.assess

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.FabricNetMessageId
import org.burstsys.fabric.net.message.{FabricNetMsg, FabricNetMsgContext, FabricNetTetherMsgType}
import org.burstsys.fabric.topology.model.node.FabricNode

trait FabricNetTetherMsg extends FabricNetMsg {

  /**
    * The timestamp the tether message was sent
    */
  def tetherSendEpoch: Long

  /**
    * The worker process id
    */
  def gitCommit: String
}

/**
  * sent from client to server to initiate a worker->supervisor relation
  */
object FabricNetTetherMsg {

  def apply(
             ruid: FabricNetMessageId,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             gitCommit: String
           ): FabricNetTetherMsg = {
    val m = FabricNetTetherMsgContext()
    m.messageId = ruid
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.tetherSendEpoch = System.currentTimeMillis
    m.gitCommit = gitCommit
    m
  }

  def apply(buffer: Array[Byte]): FabricNetTetherMsg = {
    FabricNetTetherMsgContext().decode(buffer)
  }
}


private final case
class FabricNetTetherMsgContext()
  extends FabricNetMsgContext(FabricNetTetherMsgType) with FabricNetTetherMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  var tetherSendEpoch: Long = -1

  var gitCommit: String = ""

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    tetherSendEpoch = input.readLong()
    gitCommit = input.readString()
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(tetherSendEpoch)
    output.writeString(gitCommit)
  }

}
