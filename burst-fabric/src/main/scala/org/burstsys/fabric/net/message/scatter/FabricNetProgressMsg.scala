/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.scatter

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.message.FabricNetProgressMsgType
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.uid.VitalsUid
import io.netty.buffer.ByteBuf

/**
  * a scatter message reflected back periodically to make sure
  * master/server knows progress is still being made
  */
trait FabricNetProgressMsg extends FabricNetScatterMsg {

  def eventId: Long

  def nanos: Long

  def store: String

  def storeEvent: String

  def scatterMsg: String
}

object FabricNetProgressMsg {

  def apply(
             senderKey: FabricNode,
             receiverKey: FabricNode,
             guid: VitalsUid,
             ruid: VitalsUid,
             eventId: Long,
             nanos: Long,
             store: String = null,
             storeEvent: String = null
           ): FabricNetProgressMsg = {
    val m = FabricNetProgressMsgContext()
    m.senderKey = senderKey
    m.receiverKey = receiverKey
    m.guid = guid
    m.ruid = ruid
    m.eventId = eventId
    m.nanos = nanos
    m.store = store
    m.storeEvent = storeEvent
    m
  }

  def apply(buffer: ByteBuf): FabricNetProgressMsg = {
    FabricNetProgressMsgContext().decode(buffer)
  }

}

private[net]
case class FabricNetProgressMsgContext()
  extends FabricNetScatterMsgContext(FabricNetProgressMsgType) with FabricNetProgressMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  var eventId: Long = _
  var nanos: Long = _
  var store: String = _
  var storeEvent: String = _

  ////////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ////////////////////////////////////////////////////////////////////////////////////

  override def scatterMsg: String = {
    eventId match {
      case 100 => s"$nanos | Particle start"
      case 200 => s"$nanos | Store event | $store | $storeEvent"
      case 300 => s"$nanos | Data loaded"
      case 400 => s"$nanos | Particle finish"
      case _ => s"$nanos | Unknown message $eventId"
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // link
  ////////////////////////////////////////////////////////////////////////////////////

  def link(msg: FabricNetProgressMsg): this.type = {
    super.link(msg)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    eventId = input.readLong()
    nanos = input.readLong()
    store = input.readString()
    storeEvent = input.readString()
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeLong(eventId)
    output.writeLong(nanos)
    output.writeString(store)
    output.writeString(storeEvent)
  }

}
