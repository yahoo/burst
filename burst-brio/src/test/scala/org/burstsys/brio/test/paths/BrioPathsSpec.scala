/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.paths

import org.burstsys.brio.types.{BrioPath, _}
import org.burstsys.brio.test.BrioAbstractSpec
import org.scalatest._


class BrioPathsSpec extends BrioAbstractSpec {

  "Brio Path" should "check basic logic for BrioPath" in {
    var path1 = BrioPath("testRoot")
    var path2 = path1.withLeaf("testLeaf")

    path1.depth should be(1)
    path2.depth should be(2)
    path2.parent should be(path1)
    path2.root should be(path1)
    path2.withoutRoot should be(BrioPath("testLeaf"))
    path2.max(path1) should be(path2)
    path2.min(path1) should be(path1)

  }
  "Brio Path" should "function correctly" in {

    {
      val path: BrioPath = "user.sessions"
      val depth = path.depth
      depth should equal(2)
    }

    {
      val path: BrioPath = "user.sessions.events"
      val depth = path.depth
      depth should equal(3)
    }
    /*
        val path0: BrioPath = "user.sessions"
        val accessor0 = path0.accessor
        accessor0._1 should equal("user.sessions")
        accessor0._2 should equal(None)
        accessor0._3 should equal(None)

        val path1: BrioPath = "user.sessions(sessionId)"
        val accessor1 = path1.accessor
        accessor1._1 should equal("user.sessions")
        accessor1._2 should equal(Some("sessionId"))
        accessor1._3 should equal(None)

        val path2: BrioPath = "user.sessions(sessionId).foobar"
        val accessor2 = path2.accessor
        accessor2._1 should equal("user.sessions")
        accessor2._2 should equal(Some("sessionId"))
        accessor2._3 should equal(Some("foobar"))

    */
  }


}
