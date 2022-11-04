/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.group

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.vitals.json.VitalsJsonObject
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.uid._

/**
 * The composite key for a single ''group'' execution
 */
trait FabricGroupKey extends VitalsJsonRepresentable[FabricGroupKey] {

  /**
   * the name of the group operation (this does ''not'' need to be unique across executions)
   *
   * @return
   */
  def groupName: FabricGroupName

  /**
   * the UID of the group operation (this ''does'' need to be unique across executions)
   *
   * @return
   */
  def groupUid: FabricGroupUid

  final override
  def toString: String = s"(groupName='$groupName', guid=$groupUid)"

}

object FabricGroupKey {
  def apply(
             groupName: FabricGroupName = "",
             groupUid: FabricGroupUid = newBurstUid
           ): FabricGroupKey =
    FabricGroupKeyContext(groupName, groupUid)
}

final case
class FabricGroupKeyContext(var groupName: FabricGroupName, var groupUid: FabricGroupUid)
  extends FabricGroupKey with VitalsJsonObject with KryoSerializable {

  override def toJson: FabricGroupKey = FabricGroupKeyContext(groupName, groupUid)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    groupName = input.readString
    groupUid = input.readString
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeString groupName
    output writeString groupUid
  }

}
