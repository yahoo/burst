/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.brio.provider.BrioSchemaProvider
import org.burstsys.brio.test.press.BrioMockPressSource

package object provider {


  final case class BrioUltimateProvider() extends BrioSchemaProvider[BrioMockPressSource] {

    val names: Array[String] = Array("ultimate", "Ultimate", "UltimateSchema")

    val schemaResourcePath: String = "/org/burstsys/brio/test/ultimate"

    val presserClass: Class[BrioMockPressSource] = classOf[BrioMockPressSource]

  }

  final case class BrioPressProvider() extends BrioSchemaProvider[BrioMockPressSource] {

    val names: Array[String] = Array("presser")

    val schemaResourcePath: String = "/org/burstsys/brio/test/press"

    val presserClass: Class[BrioMockPressSource] = classOf[BrioMockPressSource]

  }

  final case class BrioTestProvider() extends BrioSchemaProvider[BrioMockPressSource] {

    val names: Array[String] = Array("test")

    val schemaResourcePath: String = "/org/burstsys/brio/test/test"

    val presserClass: Class[BrioMockPressSource] = classOf[BrioMockPressSource]

  }

}
