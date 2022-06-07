/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._

import scala.language.implicitConversions
import scala.util.{Failure, Try}

package object catalog extends VitalsLogger {

  final val cannedDataLabel = "cannedData"

  final val torcherDataLabel = "torcherData"

  type BurstMoniker = String

  type BurstLabels = VitalsLabelsMap

  def resultOrFailure[T](work: => Try[T]): Try[T] = {
    try {
      work
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        Failure(t)
    }
  }
}
