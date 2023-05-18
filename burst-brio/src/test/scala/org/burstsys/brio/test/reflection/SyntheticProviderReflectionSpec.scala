/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.reflection

import org.burstsys.brio.provider.BrioSyntheticDataProvider
import org.burstsys.brio.test.BrioAbstractSpec
import org.burstsys.vitals.configuration.burstVitalsReflectionScanPrefixProperty
import org.burstsys.vitals.reflection
import org.scalatest.BeforeAndAfterAll

import scala.language.postfixOps

class SyntheticProviderReflectionSpec extends BrioAbstractSpec with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    burstVitalsReflectionScanPrefixProperty.set("test.burstsys")
  }

  it should "find classes in burst package" in {
    val scannedClasses = reflection.getSubTypesOf(classOf[BrioSyntheticDataProvider])
    scannedClasses.size should equal(3)
    scannedClasses.map(_.getName).count(_.startsWith("test")) should equal(1)
  }

}
