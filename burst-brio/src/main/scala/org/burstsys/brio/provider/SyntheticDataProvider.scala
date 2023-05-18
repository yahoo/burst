/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.provider

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.reflection

import java.util.concurrent.atomic.AtomicInteger

object SyntheticDataProvider {

  private lazy val dataProviders: Map[String, Class[_ <: BrioSyntheticDataProvider]] = {
    val scannedClasses = reflection.getSubTypesOf(classOf[BrioSyntheticDataProvider])
    scannedClasses.map(p => (p.getDeclaredConstructor().newInstance().datasetName, p)).toMap
  }

  def providerNamed(name: String): BrioSyntheticDataProvider = {
    dataProviders.get(name) match {
      case Some(value) =>
        value.getDeclaredConstructor().newInstance()
      case None =>
        log error burstStdMsg(s"Unknown dataset name: '$name'. Available datasets: ${dataProviders.keys.mkString("{'", "', '", "'}")}")
        null
    }
  }

  case class SyntheticRepeatedValue[T](data: T*) {
    private val index = new AtomicInteger(0)

    def next: T = value(index.getAndIncrement())

    def value(atIndex: Int): T = data(atIndex % data.length)
  }

}
