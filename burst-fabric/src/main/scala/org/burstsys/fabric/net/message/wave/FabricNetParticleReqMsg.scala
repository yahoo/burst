/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.wave

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.execution.model.wave.FabricParticle
import org.burstsys.fabric.net.message.FabricNetParticleReqMsgType
import org.burstsys.fabric.net.message.scatter.{FabricNetScatterMsg, FabricNetScatterMsgContext}
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.uid._
import io.netty.buffer.ByteBuf

trait FabricNetParticleReqMsg extends FabricNetScatterMsg {

  /**
    * TODO
    *
    * @return
    */
  def particle: FabricParticle

}

/**
  * sent from server to client to initiate a wave scan
  */
object FabricNetParticleReqMsg {

  def apply(guid: VitalsUid, ruid: VitalsUid, senderKey: FabricNode, receiverKey: FabricNode,
            particle: FabricParticle): FabricNetParticleReqMsg = {
    val m = FabricNetParticleReqMsgContext()
    m.guid = guid
    m.ruid = ruid
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.particle = particle
    m
  }

  def apply(buffer: ByteBuf): FabricNetParticleReqMsg = {
    FabricNetParticleReqMsgContext().decode(buffer)
  }

}

final case
class FabricNetParticleReqMsgContext()
  extends FabricNetScatterMsgContext(FabricNetParticleReqMsgType) with FabricNetParticleReqMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // STATE
  ////////////////////////////////////////////////////////////////////////////////////

  var particle: FabricParticle = _

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    particle = kryo.readClassAndObject(input).asInstanceOf[FabricParticle]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeClassAndObject(output, particle)
  }

}
