/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.nexus.stream.NexusStream
import org.burstsys.nexus.{NexusGlobalUid, NexusStreamUid}
import org.burstsys.vitals.uid._
import io.netty.buffer.ByteBuf

/**
  * An abstract message within the Nexus Protocol
  */
private[nexus]
abstract class NexusMsg(val messageType: NexusMsgType) extends AnyRef {

  private lazy val mName = getClass.getSimpleName.stripSuffix("$")
  def messageName: String = mName

  override def toString: String = s"$messageName ruid=$ruid, guid=$guid, suid=$suid"

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _guid: VitalsUid = _

  private[this]
  var _suid: NexusStreamUid = _

  private[this]
  var _ruid: Int = _

  ////////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * connection unique request id
    *
    * @return
    */
  def ruid: Int = _ruid

  /**
    * connection unique id
    *
    * @param id
    */
  def ruid(id: Int): Unit = _ruid = id

  /**
    * nexus global operation uid
    *
    * @return
    */
  def guid: NexusGlobalUid = _guid

  /**
    * nexus global operation uid
    *
    * @param id
    */
  def guid(id: NexusGlobalUid): Unit = _guid = id

  /**
    * nexus global operation uid
    *
    * @return
    */
  def suid: NexusStreamUid = _suid

  /**
    * nexus global operation uid
    *
    * @param id
    */
  def suid(id: NexusStreamUid): Unit = _suid = id

  ////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  def link(msg: NexusMsg): this.type = {
    _ruid = msg.ruid
    _guid = msg.guid
    _suid = msg.suid
    this
  }

  def link(stream: NexusStream): this.type = {
    _guid = stream.guid
    _suid = stream.suid
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  protected def decode(buffer: ByteBuf): this.type = {
    _ruid = buffer.readInt
    _guid = decodeAsciiStringFromByteBuf(buffer)
    _suid = decodeAsciiStringFromByteBuf(buffer)
    this
  }

  def encode(buffer: ByteBuf): this.type = {
    buffer.writeInt(messageType.code)
    buffer.writeInt(_ruid)
    encodeAsciiStringToByteBuf(_guid, buffer)
    encodeAsciiStringToByteBuf(_suid, buffer)
    this
  }

}
