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

  val lociCountProperty: String = "synthetic.samplestore.loci.count"

  val persistentHashProperty: String = "synthetic.samplestore.variable-hash"

  val useLocalHostProperty: String = "synthetic.samplestore.use-localhost"

  val defaultLociCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = lociCountProperty,
    description = "number of samplestore loci to generate",
    default = Some(0)
  )

  val defaultPersistentHashProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = persistentHashProperty,
    description = "whether the generated hash should change or not",
    default = Some(true)
  )

  val defaultUseLocalHostProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = useLocalHostProperty,
    description = "if the loci should use `localhost:127.0.0.1` for the hostname/ip address",
    default = Some(true)
  )

  ///////////////////////////////////////////////////////////////////
  // Data pressing
  ///////////////////////////////////////////////////////////////////

  val syntheticDatasetProperty = "synthetic.samplestore.press.dataset"

  val pressTimeoutProperty = "synthetic.samplestore.press.timeout"

  val maxItemSizeProperty = "synthetic.samplestore.press.item.max.bytes"

  val itemCountProperty = "synthetic.samplestore.press.item.count"

  val defaultPressTimeoutProperty: VitalsPropertySpecification[Duration] = VitalsPropertySpecification[Duration](
    key = pressTimeoutProperty,
    description = "the amount of time to wait for all items to be pressed",
    default = Some(1 minute)
  )

  val defaultMaxItemSizeProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = maxItemSizeProperty,
    description = "the max size for an item in bytes",
    default = Some(10e6.toInt)
  )

  val defaultItemCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification(
    key = itemCountProperty,
    description = "the number of items to generate",
    default = Some(0)
  )

}
