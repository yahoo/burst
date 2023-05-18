/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.runtime

import org.burstsys.fabric.wave.execution.model.gather.plane.FabricPlaneGather
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.kryo.FeltKryoSerializable
import org.burstsys.vitals.errors.VitalsException

abstract
class FeltCollectorGather extends FabricPlaneGather with FeltKryoSerializable {

  final override def resultMessage: String = {
    if (!hadException) "ok"
    else exceptionStack // messageFromException(exception)
  }

  final override def rowCount: Long = planes.filter(_ != null).map(_.rowCount).sum

  final override def overflowCount: Long = planes.filter(_ != null).count(_.dictionaryOverflow)

  final override def limitCount: Long = planes.filter(_ != null).count(_.rowLimitExceeded)

  final override def queryCount: Long = planes.count(_ != null)

  final override def successCount: Long = planes.filter(_ != null).count(_.resultStatus.isSuccess)

  /**
   * activate each of the gather planes - one per active plane where each plan represents a processing 'frame'.
   * There is a different type of plane for each different type of frame.
   *
   * @param builders
   * @return
   */
  @inline final
  def activatePlanesOnWorker[B <: FeltCollectorBuilder](binding: FeltBinding, builders: Array[B]): this.type = {
    if (builders.length != activePlanes)
      throw VitalsException(s"mismatch between builders and active planes (${builders.length}, $activePlanes)")
    var i = 0
    while (i < activePlanes) {
      val builder = builders(i)
      assert(builder.frameId == i)
      planes(i) = builder.newCollectorPlaneOnWorker()
      i += 1
    }
    this
  }

}
