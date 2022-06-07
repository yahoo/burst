/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.cubes

import org.burstsys.eql.canned.EqlQueriesCan
import org.burstsys.eql.test.support.EqlAlloyTestRunner

/**
  *  Tbis is the EQL attempt at the product metadata query requirement.
  *
  */
final
class EqlRepeatableSpec extends EqlAlloyTestRunner {

// We have to break the query up into parts right now since it breaks Hydra for size
  it should "generate identical hydra for same source to help with cacheing" in {
    val hydraSource1 = eql.eqlToHydra(None, EqlQueriesCan.uiUnityMetadataSourceV1)
    val hydraSource2 = eql.eqlToHydra(None, EqlQueriesCan.uiUnityMetadataSourceV1)
    hydraSource1 should equal(hydraSource2)
  }
}
