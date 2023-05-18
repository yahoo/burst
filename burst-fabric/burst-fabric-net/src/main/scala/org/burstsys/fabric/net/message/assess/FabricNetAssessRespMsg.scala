/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.assess

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.container.metrics.FabricAssessment
import org.burstsys.fabric.net.message.{FabricNetAssessRespMsgType, FabricNetMsg, FabricNetMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import io.netty.buffer.ByteBuf

trait FabricNetAssessRespMsg extends FabricNetMsg {

  /**
   * the commit id of the code this worker is running
   */
  def gitCommit: String

  /**
    * The nano timestamp (in the supervisor JVM) when the message was sent
    */
  def sentNanos: Long

  /**
    * The elapsed duration for the request (only valid in the supervisor JVM)
    */
  def elapsedNanos: Long

  /**
    * The worker container's state
    */
  def assessment: FabricAssessment

}

/**
  * sent from client to server to provide an assessment
  */
object FabricNetAssessRespMsg {

  def apply(
             req: FabricNetAssessReqMsg,
             senderKey: FabricNode,
             receiverKey: FabricNode,
             gitCommit: String,
             assessment: FabricAssessment
           ): FabricNetAssessRespMsg = {
    val m = FabricNetAssessRespMsgContext()
    m.link(req)
    m.gitCommit = gitCommit
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.assessment = assessment
    m
  }

  def apply(buffer: Array[Byte]): FabricNetAssessRespMsg = {
    FabricNetAssessRespMsgContext().decode(buffer)
  }
}

private final case
class FabricNetAssessRespMsgContext()
  extends FabricNetMsgContext(FabricNetAssessRespMsgType) with FabricNetAssessRespMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  var gitCommit: String = _

  var sentNanos: Long = _

  var assessment: FabricAssessment = _

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  override def link(msg: FabricNetMsg): this.type = {
    super.link(msg)
    sentNanos = msg.asInstanceOf[FabricNetAssessReqMsg].sentNanos
    this
  }

  // this call is only valid on the supervisor JVM
  override def elapsedNanos: Long = System.nanoTime - sentNanos

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    gitCommit = input.readString()
    sentNanos = input.readLong()
    assessment = kryo.readClassAndObject(input).asInstanceOf[FabricAssessment]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeString(gitCommit)
    output.writeLong(sentNanos)
    kryo.writeClassAndObject(output, assessment)
  }

}
