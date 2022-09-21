/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.uid

import org.burstsys.vitals.test.VitalsAbstractSpec
import org.burstsys.vitals.uid
import org.scalatest.Inspectors.forAll

import java.util.UUID

class VitalsUidSpec extends VitalsAbstractSpec {

  it should "generate new uids" in {
    val u = uid.newBurstUid
    u should have length 33
    u should startWith("B")
    forAll(u) { c => (c.isUpper || c.isDigit) shouldBe true }
  }

  it should "validate uids" in {
    // should recognize generated UIDs
    uid.isBurstUid(uid.newBurstUid) shouldBe true

    // should not recongnize UIDs that are missing the prepended `B`
    uid.isBurstUid(uid.newBurstUid.substring(1)) shouldBe false
    uid.isBurstUid("A" + uid.newBurstUid.substring(1)) shouldBe false
    uid.isBurstUid("C" + uid.newBurstUid.substring(1)) shouldBe false

    // should not recognize UUID.randomUUID()
    uid.isBurstUid(UUID.randomUUID().toString) shouldBe false
  }
}
