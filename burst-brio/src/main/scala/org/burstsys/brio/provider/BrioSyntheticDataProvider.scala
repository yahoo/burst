/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.provider

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.brio.provider.SyntheticDataProvider.SyntheticRepeatedValue
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties.VitalsRichExtendedPropertyMap
import org.burstsys.vitals.reflection

import java.util.concurrent.atomic.AtomicInteger
import scala.reflect.ClassTag


trait BrioSyntheticDataProvider {

  def data(itemCount: Int, properties: VitalsPropertyMap = Map.empty): Iterator[BrioPressInstance]

  def datasetName: String

  def schemaName: String

  def pressSource(root: BrioPressInstance): BrioPressSource

  protected def propertyAsRepeatedValue[C: ClassTag](properties: VitalsRichExtendedPropertyMap, key: String, default: Array[C]): SyntheticRepeatedValue[C] = {
    SyntheticRepeatedValue(properties.getValueOrDefault(key, default).toIndexedSeq: _*)
  }

}
