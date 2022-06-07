/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.scatter

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.message.{FabricNetMsg, FabricNetMsgContext, FabricNetMsgType}
import org.burstsys.vitals.uid._

/**
  * This class has its equality semantics defined such that an request message can be found to 'match' its response
  * message in a set.
  */
trait FabricNetScatterMsg extends FabricNetMsg {

  /**
    * TODO
    *
    * @return
    */
  def guid: VitalsUid

  /**
    * TODO
    *
    * @return
    */
  def ruid: VitalsUid

  final override def hashCode(): Int = {
    var result = 7
    result = 31 * result + guid.hashCode()
    result = 31 * result + ruid.hashCode()
    result
  }

  final override def equals(obj: Any): Boolean = obj match {
    case peer: FabricNetScatterMsg => this.guid == peer.guid && this.ruid == peer.ruid
    case _ => false
  }

}

/**
  * An abstract message within the FabricNet Protocol
  *
  * @group FabricNetMsg
  */
private[net]
abstract class FabricNetScatterMsgContext(override val messageType: FabricNetMsgType)
  extends FabricNetMsgContext(messageType) with FabricNetScatterMsg {

  override def toString: String = s"${super.toString} guid=$guid, ruid=$ruid "

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  var _guid: VitalsUid = _

  var _ruid: VitalsUid = _

  ////////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ////////////////////////////////////////////////////////////////////////////////////

  def guid: VitalsUid = _guid

  def guid_=(uid: VitalsUid): Unit = _guid = uid

  def ruid: VitalsUid = _ruid

  def ruid_=(uid: VitalsUid): Unit = _ruid = uid

  ////////////////////////////////////////////////////////////////////////////////////
  // link
  ////////////////////////////////////////////////////////////////////////////////////

  def link(msg: FabricNetScatterMsg): this.type = {
    super.link(msg)
    _guid = msg.guid
    _ruid = msg.ruid
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    _guid = input.readString()
    _ruid = input.readString()
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeString(_guid)
    output.writeString(_ruid)
  }

}
