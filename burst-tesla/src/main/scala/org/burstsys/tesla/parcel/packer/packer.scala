/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel

import java.util
import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid._
import org.jctools.queues.MpmcArrayQueue

import scala.language.postfixOps

package object packer extends VitalsLogger {

  final val packerId = new AtomicInteger()

  final val parcelMaxSize: TeslaMemorySize = (200 * MB).toInt

  final lazy val slotQueue: util.Queue[TeslaParcelPacker] = new MpmcArrayQueue[TeslaParcelPacker](1000)

  final
  def grabPacker(guid: VitalsUid, pipe: TeslaParcelPipe): TeslaParcelPacker = {
    slotQueue poll match {
      case null => TeslaParcelPackerContext().open(guid, pipe)
      case slot: TeslaParcelPackerContext => slot.open(guid, pipe)
    }
  }

  final
  def releasePacker(packer: TeslaParcelPacker): Unit = {
    packer.asInstanceOf[TeslaParcelPackerContext].close
    slotQueue add packer
  }

}
