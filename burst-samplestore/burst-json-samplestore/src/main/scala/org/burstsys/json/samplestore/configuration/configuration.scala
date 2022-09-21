/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.vitals.io._
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.properties.{VitalsPropertyKey, VitalsPropertyMap, VitalsPropertyRegistry, VitalsPropertySpecification}

package object configuration extends VitalsLogger with VitalsPropertyRegistry {
  val jsonLoadConcurrencyProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "json.samplestore.load.concurrency",
    description = "max allowed concurrent loads",
    default = Some(math.max(10, (Runtime.getRuntime.availableProcessors / 6.0).toInt))
  )

  val jsonLociCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "json.samplestore.loci.count",
    description = "number of samplestore loci to generate",
    default = Some(0)
  )

  val jsonVersionProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "json.samplestore.json.rootVersion",
    description = "the default version for Json alloy data",
    default = Some(1)
  )

  final val alloySkipIndexStreamPropertyKey: VitalsPropertyKey = "json.samplestore.stream.skipIndex"
  final val alloyLocationPropertyKey: VitalsPropertyKey = "json.samplestore.location"

  sealed trait JsonSamplestoreConfiguration extends Any {
    def distributedMode: Boolean
    def propertiesFile: String
    def properties: VitalsPropertyMap
  }


  final case
  class JsonSamplestoreDefaultConfiguration() extends JsonSamplestoreConfiguration {
    override def distributedMode: Boolean = false
    override def propertiesFile = "localAlloy.properties"
    def properties: VitalsPropertyMap = loadPropertyMapFromJavaPropertiesFile(propertiesFile)
  }

  final case
  class JsonSamplestoreDistributedConfiguration() extends JsonSamplestoreConfiguration {
    override def distributedMode: Boolean = true
    override def propertiesFile = "localAlloy.properties"
    def properties: VitalsPropertyMap = loadPropertyMapFromJavaPropertiesFile(propertiesFile)
  }

}
