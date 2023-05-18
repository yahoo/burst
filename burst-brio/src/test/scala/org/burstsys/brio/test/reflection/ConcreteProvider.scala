/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.reflection

import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.brio.provider.BrioSyntheticDataProvider
import org.burstsys.vitals.properties.VitalsPropertyMap

case class ConcreteProvider() extends BrioSyntheticDataProvider {
  override def data(itemCount: Int, properties: VitalsPropertyMap): Iterator[BrioPressInstance] = ???

  override def datasetName: String = ???

  override def schemaName: String = ???

  override def pressSource(root: BrioPressInstance): BrioPressSource = ???
}
