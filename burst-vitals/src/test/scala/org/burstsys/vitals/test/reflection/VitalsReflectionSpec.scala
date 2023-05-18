/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.reflection

import org.burstsys.vitals.configuration.burstVitalsReflectionScanPrefixProperty
import org.burstsys.vitals.reflection
import org.burstsys.vitals.test.VitalsAbstractSpec
import org.scalatest.BeforeAndAfterAll

import scala.language.postfixOps

class VitalsReflectionSpec extends VitalsAbstractSpec with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    burstVitalsReflectionScanPrefixProperty.set("test.burstsys")
  }

  it should "find classes in burst package" in {
    val scannedClasses = reflection.getSubTypesOf(classOf[TestProvider])
    scannedClasses.size should equal(2)
  }

}
