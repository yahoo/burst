/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.assess

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.FabricNetMessageId
import org.burstsys.fabric.net.message.{AccessParameters, FabricNetHeartbeatMsgType, FabricNetMsg, FabricNetMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode

trait FabricNetHeartbeatMsg extends FabricNetMsg {

  /** The timestamp the tether message was sent */
  def tetherSendEpoch: Long

  /** The worker process id */
  def gitCommit: String

  /** A way to pass info in an access info, but not too much */
  def parameters: AccessParameters

}

/**
 * sent from client to server to initiate a worker->supervisor relation
 */
object FabricNetHeartbeatMsg {

  def apply(
             ruid: FabricNetMessageId,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             gitCommit: String,
             parameters: AccessParameters,
           ): FabricNetHeartbeatMsg = {
    val m = FabricNetHeartbeatMsgContext()
    m.messageId = ruid
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.tetherSendEpoch = System.currentTimeMillis
    m.gitCommit = gitCommit
    m.parameters = parameters
    m
  }

  def apply(buffer: Array[Byte]): FabricNetHeartbeatMsg = {
    FabricNetHeartbeatMsgContext().decode(buffer)
  }
}


private final case
class FabricNetHeartbeatMsgContext()
  extends FabricNetMsgContext(FabricNetHeartbeatMsgType) with FabricNetHeartbeatMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  var tetherSendEpoch: Long = -1

  var gitCommit: String = ""

  var parameters: AccessParameters = Map.empty

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    tetherSendEpoch = input.readLong()
    gitCommit = input.readString()
    parameters = readMap(kryo, input)
  }

  override def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(tetherSendEpoch)
    output.writeString(gitCommit)
    writeMap(kryo, output, parameters.asInstanceOf[Map[String, Serializable]])
  }

}
