/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.kryo

/**
 * a marker trait that allows us using reflection to grab all kryo seralized classes,
 * with their unique keys, using reflection. This works both in supervisor and in slave/worker nodes.
 * The first 128 classes registered will be sent with 1 byte. Classes after that will be sent with 2 bytes.
 */
trait VitalsKryoCatalogProvider {


  def kryoClasses: Array[VitalsKryoClassPair]

  val commonCatalogStart = 256
  val brioCatalogStart: VitalsKryoKey = commonCatalogStart + 150
  val metricsCatalogStart: VitalsKryoKey = brioCatalogStart + 150
  val zapCatalogStart: VitalsKryoKey = metricsCatalogStart + 150
  val gistCatalogStart: VitalsKryoKey = zapCatalogStart + 150
  val hydraCatalogStart: VitalsKryoKey = gistCatalogStart + 150
  val feltCatalogStart: VitalsKryoKey = hydraCatalogStart + 150
  val unitCatalogStart: VitalsKryoKey = feltCatalogStart + 150
  val fabricCatalogStart: VitalsKryoKey = unitCatalogStart + 150
  val fabricMockCatalogStart: VitalsKryoKey = fabricCatalogStart + 150
  val fabricSampleStoreStart: VitalsKryoKey = fabricMockCatalogStart + 150
  val fuseSampleStoreStart: VitalsKryoKey = fabricSampleStoreStart + 150

}
