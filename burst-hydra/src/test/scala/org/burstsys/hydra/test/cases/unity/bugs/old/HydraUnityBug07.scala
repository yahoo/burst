/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.bugs.old

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * // TODO Hydra bug
 * it should "successfully do a single cube query with dimensions and aggregates at different levels query" in {
 * val source =
 * s"""
 * |select count(user) as userCount, count(user.sessions.events) as eventCount,
 * |  day(1) as 'day', 1 as 'dashboard'
 * |from schema unity
 * """.stripMargin
 * Expected :Array((50,12500,-57600000,1))
 * Actual   :Array((0,12500,0,0), (50,0,-57600000,1))
 */
object HydraUnityBug07 extends HydraUseCase(100, 200, "unity") {

  //  override val sweep = new B04102CF699BA4F12B7F8C295E75A6C0A

  override val serializeTraversal = true

  override val analysisSource: String =
    s"""|
        |hydra $analysisName() {
        |	schema unity
        |	frame $frameName {
        |		cube user {
        |			limit = 100
        |			aggregates {
        |				'eventCount':sum[long]
        |				'userCount':sum[long]
        |			}
        |			dimensions {
        |				'day':dayGrain[long]
        |				'dashboard':verbatim[byte]
        |			}
        |		}
        |		user => {
        |			pre => 			{
        |				$analysisName.$frameName.day = 1
        |				$analysisName.$frameName.dashboard = 1
        |			}
        |			post => 			{
        |				$analysisName.$frameName.userCount = 1
        |			}
        |		}
        |		user.sessions.events => {
        |			post => 			{
        |				$analysisName.$frameName.eventCount = 1
        |			}
        |		}
        |	}
        |}
        |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val found = r.rowSet.map {
      row =>
        (
          row(r.columnNamesMap("userCount")).asLong,
          row(r.columnNamesMap("eventCount")).asLong,
          row(r.columnNamesMap("day")).asLong,
          row(r.columnNamesMap("dashboard")).asByte
        )
    }.sortBy(_._1)
    found should equal(shouldBe)
  }

  //Actual   :Array((0,0,12500,0), (-57600000,1,0,50))


  val was: Array[Any] = Array(
    (0, 12500, 0, 0),
    (50, 0, -57600000, 1)
  )

  val shouldBe: Array[Any] = Array(
    (50, 12500, -57600000, 1)
  )


}
