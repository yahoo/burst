/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.gather.plane

import com.esotericsoftware.kryo.KryoSerializable
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.fabric.execution.FabricResourceHolder
import org.burstsys.fabric.execution.model.gather.FabricMerge
import org.burstsys.fabric.execution.model.gather.metrics.FabricOutcome
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.vitals.uid._

/**
 * A Fabric Gather has one or more parallel planes of data returned - one from each active cube.
 * A single plane is stored here.
 */
abstract
class FabricPlane extends FabricOutcome
  with FabricMerge with KryoSerializable with FabricResourceHolder {

  @transient implicit lazy
  val text: VitalsTextCodec = VitalsTextCodec()

  /**
   * the user friendly name for this plane
   *
   * @return
   */
  def planeName: String

  /**
   * ordinal index for this plane
   *
   * @return
   */
  def planeId: Int

  /**
   * did the result set overflow?
   *
   * @return
   */
  def rowLimitExceeded: Boolean

  /**
   * total row count
   *
   * @return
   */
  def rowCount: Int

  /**
   * TODO
   *
   * @return
   */
  def planeDictionary: BrioMutableDictionary

  def planeDictionary_=(d: BrioMutableDictionary): Unit

  /**
   * did the dictionary overflow?
   *
   * @return
   */
  def dictionaryOverflow: Boolean

  /**
   * clear this reusable object
   */
  def clear(): Unit

}
