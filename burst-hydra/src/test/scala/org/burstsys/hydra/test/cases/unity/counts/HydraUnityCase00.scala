/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.counts

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityCase00 extends HydraUseCase(666, 666, "unity") {

  // override lazy val sweep: HydraSweep = new BCA4E9F234994458CA9BC01262E59F4DB

  override val frameSource: String =
    s"""
         frame myCube {
           cube user {
             limit = 1
             aggregates {
               userCount:sum[long]
               sessionCount:sum[long]
               eventCount:sum[long]
             }
           }
           user => {
             pre => {
               myCube.userCount = 1
             }
           }
           user.sessions => {
             pre => {
               myCube.sessionCount = 1
             }
           }
           user.sessions.events => {
             pre => {
               myCube.eventCount = 1
             }
           }
         }
  """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {

  }
}
