/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.model

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.test.BrioAbstractSpec

class BrioModelSpec extends BrioAbstractSpec {

  it should "build ultimate model" in {
    val schema = BrioSchema("ultimate")
    schema.name should equal("UltimateSchema")
    schema.aliasedTo("Ultimate") should be (true)
    schema.rootRelationName should equal("user")
    schema.rootStructureType should equal("User")
    schema.versionCount should equal(2)
  }

  it should "build test model" in {
    val schema = BrioSchema("test")
    schema.name should equal("BrioTest")
  }

}
