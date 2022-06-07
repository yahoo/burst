/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.briofuncs

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoBrioSize extends HydraUseCase(1, 1, "quo") {

  //      override val sweep = new B03D356642AFC4E23993F20784AAE1C4B

  override val frameSource: String =
    s"""
      frame $frameName {
        cube user {
          limit = 835
          dimensions {
            eventsSize:verbatim[long]
          }
        }

        user.sessions ⇒ {
          pre ⇒ {
            $analysisName.$frameName.eventsSize = size(user.sessions.events)
            insert($analysisName.$frameName)
          }
        }
      }
  """.stripMargin


  override def
  validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)

    val rows = found(r.rowSet)
    rows should equal(expected)
  }

  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => row.cells(0).asLong
    }.sorted
  }

  val expected: Array[Any] =
    Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 118, 119, 120, 121, 122, 124, 125, 126, 127, 129, 130, 131, 132, 133, 135, 137, 139, 140, 143, 144, 146, 151, 153, 154, 155, 156, 158, 162, 163, 164, 165, 166, 167, 168, 171, 172, 174, 175, 176, 177, 178, 180, 181, 182, 183, 185, 188, 192, 193, 194, 195, 196, 197, 202, 203, 204, 205, 206, 210, 211, 212, 213, 214, 217, 218, 219, 220, 221, 222, 223, 224, 226, 227, 228, 230, 232, 238, 240, 241, 242, 244, 245, 246, 248, 251, 253, 257, 260, 261, 264, 268, 271, 274, 279, 280, 282, 283, 284, 288, 289, 290, 293, 295, 298, 303, 304, 308, 315, 322, 328, 329, 330, 338, 340, 348, 350, 351, 356, 357, 359, 360, 362, 368, 370, 372, 377, 378, 382, 385, 387, 388, 389, 392, 397, 403, 404, 418, 419, 422, 430, 436, 442, 443, 444, 450, 463, 465, 466, 478, 479, 480, 488, 508, 509, 521, 524, 525, 526, 532, 537, 542, 543, 548, 552, 554, 556, 557, 565, 571, 578, 585, 586, 588, 590, 599, 605, 613, 624, 634, 635, 648, 653, 662, 663, 664, 668, 669, 673, 680, 687, 688, 713, 733, 734, 756, 758, 761, 780, 781, 782, 802, 808, 820, 857, 861, 865, 879, 889, 897, 900, 930, 933, 945, 971, 996, 998, 999, 1000)


}
