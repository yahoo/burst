/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.model

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties.{VitalsPropertyMap, readPropertyMapFromKryo, writePropertyMapToKryo}
import org.burstsys.vitals.uid._

trait SampleStoreLocus extends Any {

  /**
   * stream UID
   *
   * @return
   */
  def suid: VitalsUid

  /**
   * the remote sample store worker hostname
   *
   * @return
   */
  def hostName: VitalsHostName

  /**
   * the remote sample store worker address
   *
   * @return
   */
  def hostAddress: VitalsHostAddress

  /**
   * the remote sample store worker port
   *
   * @return
   */
  def port: VitalsHostPort

  /**
   * property list for the sample store partition
   *
   * @return
   */
  def partitionProperties: VitalsPropertyMap

}

object SampleStoreLocus {
  def apply(
             suid: VitalsUid,
             hostName: VitalsHostName,
             hostAddress: VitalsHostAddress,
             port: VitalsHostPort,
             partitionProperties: VitalsPropertyMap
           ): SampleStoreLocus =
    SampleStoreLocusContext(
      suid: VitalsUid,
      hostName: VitalsHostName,
      hostAddress: VitalsHostAddress,
      port: VitalsHostPort,
      partitionProperties: VitalsPropertyMap)
}

final case
class SampleStoreLocusContext(var suid: VitalsUid, var hostName: VitalsHostName, var hostAddress: VitalsHostAddress,
                              var port: VitalsHostPort, var partitionProperties: VitalsPropertyMap
                             ) extends KryoSerializable with SampleStoreLocus {


  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    suid = input.readString
    hostName = input.readString
    hostAddress = input.readString
    port = input.readInt
    partitionProperties = readPropertyMapFromKryo(input)
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(suid)
    output.writeString(hostName)
    output.writeString(hostAddress)
    output.writeInt(port)
    writePropertyMapToKryo(output, partitionProperties)
  }
}
