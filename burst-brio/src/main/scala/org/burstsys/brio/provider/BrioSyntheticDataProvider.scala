/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.provider

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.brio.provider.BrioSyntheticDataProvider.SyntheticRepeatedValue
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties.VitalsRichExtendedPropertyMap
import org.burstsys.vitals.reflection

import java.util.concurrent.atomic.AtomicInteger
import scala.reflect.ClassTag

object BrioSyntheticDataProvider {

  private lazy val dataProviders: Map[String, Class[_ <: BrioSyntheticDataProvider]] = {
    val scannedClasses = reflection.getSubTypesOf(classOf[BrioSyntheticDataProvider])
    scannedClasses.map(p => (p.getDeclaredConstructor().newInstance().datasetName, p)).toMap
  }

  def providerNamed(name: String): BrioSyntheticDataProvider = {
    dataProviders.get(name) match {
      case Some(value) => value.getDeclaredConstructor().newInstance()
      case None => throw VitalsException(s"Unknown dataset name: '$name'. Available datasets: ${dataProviders.keys.mkString("{'", "', '", "'}")}")
    }
  }

  case class SyntheticRepeatedValue[T](data: T*) {
    private val index = new AtomicInteger(0)

    def next: T = value(index.getAndIncrement())

    def value(atIndex: Int): T = data(atIndex % data.length)
  }

}

trait BrioSyntheticDataProvider {

  def data(itemCount: Int, properties: VitalsPropertyMap = Map.empty): Iterator[BrioPressInstance]

  def datasetName: String

  def schemaName: String

  def pressSource(root: BrioPressInstance): BrioPressSource

  protected def propertyAsRepeatedValue[C: ClassTag](properties: VitalsRichExtendedPropertyMap, key: String, default: Array[C]): SyntheticRepeatedValue[C] = {
    SyntheticRepeatedValue(properties.getValueOrDefault(key, default).toIndexedSeq: _*)
  }

}
