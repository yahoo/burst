/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.data

import com.esotericsoftware.kryo.KryoSerializable
import org.burstsys.brio.blob.BrioSlice
import org.burstsys.fabric.data.model.slice.FabricSliceKey
import org.burstsys.fabric.data.model.slice.region.hose.FabricWriteMetrics
import org.burstsys.fabric.data.model.snap.{FabricSnap, FabricSnapComponent}

/**
 * The '''actual data''' of a 'data partition' in the Burst dataset world. These are distributed/hosted
 * across worker nodes.
 */
trait FabricSliceData extends AnyRef with BrioSlice with FabricSnapComponent with FabricSliceWriteApi
  with FabricSliceDataApi with FabricSliceStateApi with FabricWriteMetrics {

  // these are not part of FabricWriteMetrics because both FabricSliceData and FabricRegion use FabricWriteMetrics
  /**
   * @return the number of regions in this slice
   */
  def regionCount: Int

  /**
   * @return simple ratio of fastest/slowest hose time
   */
  def timeSkew: Double

}

object FabricSliceData {
  def apply(snap: FabricSnap): FabricSliceData = FabricSliceDataContext(snap: FabricSnap)
}

private[fabric] final case
class FabricSliceDataContext(var snap: FabricSnap) extends FabricSliceData
  with FabricSliceReader with FabricSliceWriter with FabricSliceImage with KryoSerializable {

  // can't be lazy val because isOpen, inMemory, onDisk, are state not attributes
  def parameters: String = s"guid=${snap.guid}, sliceKey=$sliceKey, sliceIsOpen=$isOpenForWrites sliceInMemory=$sliceInMemory, sliceOnDisk=$sliceOnDisk"

  def this() = this(null)

  override def sliceKey: FabricSliceKey = snap.metadata.sliceKey

}
