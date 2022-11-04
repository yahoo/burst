/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyKey

import scala.language.postfixOps

package object torquemada extends VitalsLogger {

  val defaultQuery: Map[String, String] = Map(
    "unity" ->
      """
        | select count(user) as users
        | from schema unity
      """.stripMargin,
    "quo" ->
      """
        |select count(user) as users
        |from schema quo
      """.stripMargin
  )

  val defaultViewMotif: String =
    """
      | VIEW template {
      |   INCLUDE user
      | }
    """.stripMargin

  val defaultViewLabels = Map(catalog.torcherDataLabel -> "true")

  val defaultViewStoreProperties: Map[VitalsPropertyKey, String] = Map(
    FabricStoreNameProperty -> "sample",
    SampleStoreSourceNameProperty -> "AppEventsBrio",
    SampleStoreSourceVersionProperty -> "0.0"
  )

  val torcherProvince = "Torcher"

}
