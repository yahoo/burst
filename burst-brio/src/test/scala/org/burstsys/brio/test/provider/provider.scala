/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.brio.provider.BrioSchemaProvider

package object provider {


  final case class BrioUltimateProvider() extends BrioSchemaProvider {

    val names: Array[String] = Array("ultimate", "Ultimate", "UltimateSchema")

    val schemaResourcePath: String = "/org/burstsys/brio/test/ultimate"

  }

  final case class BrioPressProvider() extends BrioSchemaProvider {

    val names: Array[String] = Array("presser")

    val schemaResourcePath: String = "/org/burstsys/brio/test/press"

  }

  final case class BrioTestProvider() extends BrioSchemaProvider {

    val names: Array[String] = Array("test")

    val schemaResourcePath: String = "/org/burstsys/brio/test/test"

  }

}
