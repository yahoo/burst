/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.message

import org.burstsys.brio.types.BrioTypes.BrioSchemaName
import org.burstsys.nexus.{NexusRequestUid, NexusSliceKey, NexusStreamUid}
import org.burstsys.vitals.uid._
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.properties._
import io.netty.buffer.ByteBuf


/**
  * sent from client to server to request a new stream
  */
object NexusStreamInitiateMsg {

  def apply(ruid: NexusRequestUid, guid: VitalsUid, suid: NexusStreamUid, properties: VitalsPropertyMap, schema: BrioSchemaName,
            filter: BurstMotifFilter, sliceKey: NexusSliceKey, clientHostname: VitalsHostName, serverHostname: VitalsHostName): NexusStreamInitiateMsg = {
    val m = new NexusStreamInitiateMsg()
    m.ruid(ruid)
    m.guid(guid)
    m.suid(suid)
    m.properties(properties)
    m.schema(schema)
    m.filter(filter)
    m.sliceKey(sliceKey)
    m.clientHostname(clientHostname)
    m.serverHostname(serverHostname)
    m
  }

  def apply(buffer: ByteBuf): NexusStreamInitiateMsg = {
    new NexusStreamInitiateMsg().decode(buffer)
  }
}

/**
  * sent from client to server to request a new stream
  */
final
class NexusStreamInitiateMsg() extends NexusMsg(NexusStreamInitiateMsgType) {

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _benchmarkSize: Long = _

  private[this]
  var _properties: VitalsPropertyMap = _

  private[this]
  var _schema: BrioSchemaName = _

  private[this]
  var _filter: BurstMotifFilter = _

  private[this]
  var _sliceKey: NexusSliceKey = _

  private[this]
  var _clientHostname: VitalsHostName = _

  private[this]
  var _serverHostname: VitalsHostName = _

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////


  def properties: VitalsPropertyMap = _properties

  def schema: BrioSchemaName = _schema

  def filter: BurstMotifFilter = _filter

  def sliceKey: NexusSliceKey = _sliceKey

  def clientHostname: VitalsHostName = _clientHostname

  def serverHostname: VitalsHostName = _serverHostname

  def properties(p: VitalsPropertyMap): Unit = _properties = p

  def schema(s: BrioSchemaName): Unit = _schema = s

  def filter(f: BurstMotifFilter): Unit = _filter = f

  def sliceKey(s: NexusSliceKey): Unit = _sliceKey = s

  def clientHostname(h: VitalsHostName): Unit = _clientHostname = h

  def serverHostname(h: VitalsHostName): Unit = _serverHostname = h

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def decode(buffer: ByteBuf): this.type = {
    super.decode(buffer)
    _properties = decodePropertyMapToByteBuf(buffer)
    _schema = decodeUtf8StringFromByteBuf(buffer)
    _filter = decodeOptionalUtf8StringFromByteBuf(buffer)
    _sliceKey = buffer.readLong()
    _clientHostname = decodeUtf8StringFromByteBuf(buffer)
    _serverHostname = decodeUtf8StringFromByteBuf(buffer)
    this
  }

  override
  def encode(buffer: ByteBuf): this.type = {
    super.encode(buffer)
    encodePropertyMapFromByteBuf(_properties, buffer)
    encodeUtf8StringToByteBuf(_schema, buffer)
    encodeOptionalUtf8StringToByteBuf(_filter, buffer)
    buffer.writeLong(_sliceKey)
    encodeUtf8StringToByteBuf(_clientHostname, buffer)
    encodeUtf8StringToByteBuf(_serverHostname, buffer)
    this
  }

}
