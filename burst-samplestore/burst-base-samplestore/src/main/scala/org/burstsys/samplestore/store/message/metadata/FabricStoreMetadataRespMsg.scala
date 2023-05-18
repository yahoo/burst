/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.message.metadata

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.net.message.{FabricNetMsg, FabricNetMsgContext, FabricNetRespMsg}
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.samplestore.store.message.FabricStoreMetadataRespMsgType
import org.burstsys.vitals.time.VitalsMs

trait FabricStoreMetadataRespMsg extends FabricNetMsg with FabricNetRespMsg[String]

/**
 * sent from client to server to acknowledge completion of metadata update to worker
 */
object FabricStoreMetadataRespMsg {
  def apply(request: FabricStoreMetadataReqMsg, senderKey: FabricNode, receiverKey: FabricNode,
            timestamp: VitalsMs): FabricStoreMetadataRespMsg = {
    val m = FabricStoreMetadataRespMsgContext()
    link(request, senderKey, receiverKey, m)
    m.success(request.sourceName)
  }

  def apply(request: FabricStoreMetadataReqMsg, senderKey: FabricNode, receiverKey: FabricNode,
            exception: Throwable): FabricStoreMetadataRespMsg = {
    val m = FabricStoreMetadataRespMsgContext()
    link(request, senderKey, receiverKey, m)
    m.failure(exception)
  }

  private def link(request: FabricStoreMetadataReqMsg, senderKey: FabricNode, receiverKey: FabricNode, m: FabricStoreMetadataRespMsgContext): Unit = {
    m.link(request)
    m.senderKey = senderKey
    m.receiverKey = receiverKey
  }

  def apply(buffer: Array[Byte]): FabricStoreMetadataRespMsg = {
    FabricStoreMetadataRespMsgContext().decode(buffer)
  }

}

final case
class FabricStoreMetadataRespMsgContext()
  extends FabricNetMsgContext(FabricStoreMetadataRespMsgType) with FabricStoreMetadataRespMsg {

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////
  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    readResponse(kryo, input)
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    writeResponse(kryo, output)
  }

}
