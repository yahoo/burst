/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.magic

import org.burstsys.brio
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.collectors.cube.generate.calculus.FeltCubeCalculus
import org.burstsys.vitals.logging._
import org.scalatest.Ignore
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

@Ignore
class ZapCalculusSpec extends AnyFlatSpec with Matchers {

  VitalsLog.configureLogging("zap", true)
  brio.provider.loadBrioSchemaProviders()

  private lazy val brioSchema: BrioSchema = BrioSchema("quo")

  private val userKey: BrioPathKey = brioSchema.nodeForPathName("user").pathKey
  private val sessionsKey: BrioPathKey = brioSchema.nodeForPathName("user.sessions").pathKey
  private val eventsKey: BrioPathKey = brioSchema.nodeForPathName("user.sessions.events").pathKey
  private val parametersKey: BrioPathKey = brioSchema.nodeForPathName("user.sessions.events.parameters").pathKey

  it should "do zero level join" in {
    val rootDecl = Cube(
      "user",
      Array(
        Dim("d0", 0)
      ),
      Array(
        Agg("a0", 1))
    )
    val calculus = FeltCubeCalculus(rootDecl)
    val joins = calculus.joins
    joins.length should equal(0)

    assert(!calculus.isChildJoinAt(userKey))
    assert(!calculus.isChildJoinAt(sessionsKey))
    assert(!calculus.isChildJoinAt(eventsKey))
    assert(!calculus.isChildJoinAt(parametersKey))

    assert(calculus.isChildMergeAt(userKey))
    assert(calculus.isChildMergeAt(sessionsKey))
    assert(calculus.isChildMergeAt(eventsKey))
    assert(calculus.isChildMergeAt(parametersKey))
  }

  it should "do one level join" in {
    val rootDecl = Cube(
      "user",
      Array(
        Dim("d0", 0)
      ),
      Array(
        Agg("a0", 1)),
      Array(
        Cube(
          "user.sessions",
          Array(
            Dim("d1", 2)
          ),
          Array(
            Agg("a1", 3)
          )
        )
      )
    )

    val calculus = FeltCubeCalculus(rootDecl)

    assert(!calculus.isChildJoinAt(userKey))
    assert(calculus.isChildJoinAt(sessionsKey))
    assert(!calculus.isChildJoinAt(eventsKey))
    assert(!calculus.isChildJoinAt(parametersKey))

    assert(calculus.isChildMergeAt(userKey))
    assert(!calculus.isChildMergeAt(sessionsKey))
    assert(calculus.isChildMergeAt(eventsKey))
    assert(calculus.isChildMergeAt(parametersKey))

    val joins = calculus.joins
    joins.length should equal(1)
    val tst = joins.head

    tst.childCube.pathName should equal("user.sessions")
    tst.childCube.aggregationMask.data.toBinaryString should equal("10")
    tst.childCube.dimensionMask.data.toBinaryString should equal("10")

    tst.parentCube.pathName should equal("user")
    tst.parentCube.aggregationMask.data.toBinaryString should equal("11")
    tst.parentCube.dimensionMask.data.toBinaryString should equal("11")

    val merges = calculus.merges
    merges.length should equal(9)
    val j0 = calculus.childMergeAt(brioSchema.nodeForPathName("user.sessions.events").pathKey).get

    j0.cube.pathName should equal("user.sessions")
    j0.cube.aggregationMask.data.toBinaryString should equal("10")
    j0.cube.dimensionMask.data.toBinaryString should equal("10")

  }

  it should "do three level join" in {
    val rootDecl = Cube(
      "user",
      Array(
        Dim("d0", 0)
      ),
      Array(
        Agg("a0", 1)),
      Array(
        Cube(
          "user.sessions",
          Array(
            Dim("d1", 2)
          ),
          Array(
            Agg("a1", 3)
          ),
          Array(
            Cube(
              "user.sessions.events",
              Array(
                Dim("d2", 4)
              ),
              Array(
                Agg("a2", 5)
              )
            )
          )
        )
      )
    )

    val calculus = FeltCubeCalculus(rootDecl)
    calculus.joins.length should equal(2)

    assert(!calculus.isChildJoinAt(userKey))
    assert(calculus.isChildJoinAt(sessionsKey))
    assert(calculus.isChildJoinAt(eventsKey))
    assert(!calculus.isChildJoinAt(parametersKey))

    assert(calculus.isChildMergeAt(userKey))
    assert(!calculus.isChildMergeAt(sessionsKey))
    assert(!calculus.isChildMergeAt(eventsKey))
    assert(calculus.isChildMergeAt(parametersKey))

    val j0 = calculus.childJoinAt(sessionsKey).get

    j0.childCube.pathName should equal("user.sessions")
    j0.childCube.aggregationMask.data.toBinaryString should equal("110")
    j0.childCube.dimensionMask.data.toBinaryString should equal("110")

    j0.parentCube.pathName should equal("user")
    j0.parentCube.aggregationMask.data.toBinaryString should equal("111")
    j0.parentCube.dimensionMask.data.toBinaryString should equal("111")

    val j1 = calculus.childJoinAt(brioSchema.nodeForPathName("user.sessions.events").pathKey).get

    j1.childCube.pathName should equal("user.sessions.events")
    j1.childCube.aggregationMask.data.toBinaryString should equal("100")
    j1.childCube.dimensionMask.data.toBinaryString should equal("100")

    j1.parentCube.pathName should equal("user.sessions")
    j1.parentCube.aggregationMask.data.toBinaryString should equal("110")
    j1.parentCube.dimensionMask.data.toBinaryString should equal("110")
  }

  it should "do four level join" in {
    val rootDecl = Cube(
      "user",
      Array(
        Dim("d0", 0)
      ),
      Array(
        Agg("a0", 1)),
      Array(
        Cube(
          "user.sessions",
          Array(
            Dim("d1", 2)
          ),
          Array(
            Agg("a1", 3)
          ),
          Array(
            Cube(
              "user.sessions.events",
              Array(
                Dim("d2", 4)
              ),
              Array(
                Agg("a2", 5)
              ),
              Array(
                Cube(
                  "user.sessions.events.parameters",
                  Array(
                    Dim("d3", 6)
                  ),
                  Array(
                    Agg("a3", 7)
                  )
                )
              )
            )
          )
        )
      )
    )

    val calculus = FeltCubeCalculus(rootDecl)
    calculus.joins.length should equal(3)

    assert(!calculus.isChildJoinAt(userKey))
    assert(calculus.isChildJoinAt(sessionsKey))
    assert(calculus.isChildJoinAt(eventsKey))
    assert(calculus.isChildJoinAt(parametersKey))

    assert(calculus.isChildMergeAt(userKey))
    assert(!calculus.isChildMergeAt(sessionsKey))
    assert(!calculus.isChildMergeAt(eventsKey))
    assert(!calculus.isChildMergeAt(parametersKey))

    val j1 = calculus.childJoinAt(sessionsKey).get
    j1.childCube.pathName should equal("user.sessions")
    j1.childCube.aggregationMask.data.toBinaryString should equal("1110")
    j1.childCube.dimensionMask.data.toBinaryString should equal("1110")
    j1.parentCube.pathName should equal("user")
    j1.parentCube.aggregationMask.data.toBinaryString should equal("1111")
    j1.parentCube.dimensionMask.data.toBinaryString should equal("1111")

    val j2 = calculus.childJoinAt(eventsKey).get

    j2.childCube.pathName should equal("user.sessions.events")
    j2.childCube.aggregationMask.data.toBinaryString should equal("1100")
    j2.childCube.dimensionMask.data.toBinaryString should equal("1100")
    j2.parentCube.pathName should equal("user.sessions")
    j2.parentCube.aggregationMask.data.toBinaryString should equal("1110")
    j2.parentCube.dimensionMask.data.toBinaryString should equal("1110")

    val j3 = calculus.childJoinAt(parametersKey).get
    j3.childCube.pathName should equal("user.sessions.events.parameters")
    j3.childCube.aggregationMask.data.toBinaryString should equal("1000")
    j3.childCube.dimensionMask.data.toBinaryString should equal("1000")
    j3.parentCube.pathName should equal("user.sessions.events")
    j3.parentCube.aggregationMask.data.toBinaryString should equal("1100")
    j3.parentCube.dimensionMask.data.toBinaryString should equal("1100")
  }


}
