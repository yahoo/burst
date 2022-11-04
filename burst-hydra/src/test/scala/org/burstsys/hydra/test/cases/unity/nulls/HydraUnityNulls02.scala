/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.nulls

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

/**
 * determinative null comparison
 */
object HydraUnityNulls02 extends HydraUseCase(200, 200, "unity") {

  //  override val sweep = new B38B8E377B3E6467ABD1A2B5DAD0D589B

  //  override val serializeTraversal = true

  override val frameSource: String =
    s"""|
        |  frame $frameName {
        |      var T2:boolean=null // frame global variable
        |      cube user {
        |         limit = 100
        |         dimensions {
        |            test1:verbatim[byte]
        |            test2:verbatim[byte]
        |            test3:verbatim[byte]
        |         }
        |      }
        |      user.sessions => {
        |         pre => {
        |            if(T2 == null) {
        |               $analysisName.$frameName.test1 = 1
        |               T2 = true
        |            } else if(T2 != null) {
        |               $analysisName.$frameName.test2 = 1
        |               T2 = null
        |            } else {
        |               // should not be called ever...
        |               $analysisName.$frameName.test3 = 1
        |            }
        |            insert($analysisName.$frameName)
        |         }
        |      }
        |   }
        |""".stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    val names = result.resultSets(0).columnNamesMap
    val expected = result.resultSets(0).rowSet.map {
      row => (row(names("test1")).asByte, row(names("test2")).asByte, row(names("test3")).asByte)
    }.sortBy(_._1).sortBy(_._2).sortBy(_._3)

    expected should equal(
      Array((1,0,0), (0,1,0))
    )
  }


}
