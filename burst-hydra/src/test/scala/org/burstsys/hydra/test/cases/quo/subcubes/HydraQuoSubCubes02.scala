/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.subcubes

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoSubCubes02 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep = new B1C08A1C03526415D8FDC02C521266354

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 20
          aggregates {
            userCount:sum[long]
          }
          dimensions {
            gender:verbatim[byte]
          }
          cube user.segments {
            dimensions {
              segmentId:verbatim[long]
            }
          }
        }

        user ⇒ {
          pre ⇒ {
            $analysisName.$frameName.gender = user.'project'.gender
            $analysisName.$frameName.userCount = 1
          }
        }

        user.segments ⇒ {
          pre ⇒ {
            $analysisName.$frameName.segmentId = user.segments.segmentId
            insert($analysisName.$frameName)
          }
        }

      }
  """.stripMargin


  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    found(r.rowSet) should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row =>
        (
          row.cells(0).asByte match {
            case -1 => "Unknown"
            case 0 => "Male"
            case 1 => "Female"
            case _ => ???
          },
          if (row.cells(1).isNull) -1 else row.cells(1).asLong,
          row.cells(2).asLong
        )
    }.sortBy(_._2).sortBy(_._1)
  }

  val expected: Array[Any] = Array(("Female",19184,9), ("Female",19192,3), ("Female",19272,3), ("Female",20129,8), ("Male",19184,12), ("Male",19187,14), ("Male",19192,4), ("Male",19272,7), ("Male",20129,11), ("Unknown",19184,2), ("Unknown",19272,2), ("Unknown",20129,2))


}
