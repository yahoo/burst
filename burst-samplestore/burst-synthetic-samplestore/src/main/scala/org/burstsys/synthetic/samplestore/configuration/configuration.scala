/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore

import org.burstsys.vitals.properties.VitalsPropertySpecification

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

package object configuration {

  ///////////////////////////////////////////////////////////////////
  // Slice Generation
  ///////////////////////////////////////////////////////////////////

  val persistentHashProperty: String = "synthetic.samplestore.variable-hash"
  val defaultPersistentHashProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = persistentHashProperty,
    description = "whether the generated hash should change or not",
    default = Some(true)
  )

  ///////////////////////////////////////////////////////////////////
  // Data pressing
  ///////////////////////////////////////////////////////////////////

  val syntheticDatasetProperty = "synthetic.samplestore.press.dataset"

  val pressTimeoutProperty = "synthetic.samplestore.press.timeout"
  val defaultPressTimeoutProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = pressTimeoutProperty,
    description = "the amount of time to wait for all items to be pressed",
    default = Some(1 minute)
  )

  val maxItemSizeProperty = "synthetic.samplestore.press.item.max.bytes"
  val defaultMaxItemSizeProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = maxItemSizeProperty,
    description = "the max size for an item in bytes",
    default = Some(10e6.toInt)
  )

  val itemCountProperty = "synthetic.samplestore.press.item.count"
  val defaultItemCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = itemCountProperty,
    description = "the number of items to generate",
    default = Some(0)
  )

}
