/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.test.besides

import org.burstsys.brio.types.BrioTypes
import org.burstsys.eql.canned.EqlQueriesCan
import org.burstsys.eql.test.support.EqlAlloyTestRunner
import org.burstsys.vitals.errors.VitalsException

/**
  *  Tbis is the EQL attempt at the product metadata query requirement.
  *
  */
final
class EqlMetadataSpec extends EqlAlloyTestRunner {

// We have to break the query up into parts right now since it breaks Hydra for size
  it should "run the v1 UI metadata query" in {
    // goes in the event parameerKey query
    runTest(EqlQueriesCan.uiUnityMetadataSourceV1, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected: Array[Any] = Array(
        Array(Array(50)),
        Array(Array(1250)),
        Array(Array(12500)),
        Array(Array(1483261260001L)),
        Array(Array(1483261260050L)),
        Array(Array(1514797260001L)),
        Array(Array(1514797273740L)),
        Array(Array(555666, 16), Array(666777, 17), Array(888999, 17)),
        Array(Array(232323, 325), Array(454545, 300), Array(676767, 325), Array(898989, 300)),
        Array(Array(111222, 10), Array(333444, 10), Array(555666, 10), Array(777888, 10), Array(888999, 10)),
        Array(Array(12121212, 50), Array(13131313, 50), Array(101010101, 50), Array(1414141414, 50), Array(1515151515, 50), Array(1616161616, 50)),
        Array(Array(-1202440757, 250), Array(-1202440756, 250), Array(-1202440755, 250), Array(-1202440754, 250), Array(-1202440753, 250)),
        Array(Array(9876, 416), Array(54321, 417), Array(54329, 417)),
        Array(Array(983, 250), Array(984, 250), Array(985, 250), Array(986, 250), Array(987, 250)),
        Array(Array(12, 156), Array(13, 157), Array(14, 157), Array(15, 156), Array(16, 156), Array(17, 156),
          Array(18, 156), Array(19, 156)),
        Array(Array(1, 1136), Array(2, 1137), Array(3, 1137), Array(4, 1137), Array(5, 1137), Array(6, 1136),
          Array(7, 1136), Array(8, 1136), Array(9, 1136), Array(10, 1136), Array(11, 1136)),
        Array(Array(-1, 1250)),
        Array(Array(22, 16), Array(33, 17), Array(44, 17)),
        Array(Array(-1, 50)),
        Array(Array(82137, 892), Array(82138, 893), Array(82139, 893), Array(82140, 893), Array(82141, 893), Array(82142, 893), Array(82143, 893)),
        Array(Array(1, 68683, 810), Array(1, 68684, 811), Array(1, 68685, 812), Array(1, 68686, 812), Array(1, 68687, 812),
          Array(1, 68688, 812), Array(1, 68689, 811), Array(2, 68683, 812), Array(2, 68684, 813), Array(2, 68685, 813),
          Array(2, 68686, 813), Array(2, 68687, 812), Array(2, 68688, 811), Array(2, 68689, 811), Array(3, 68683, 813),
          Array(3, 68684, 813), Array(3, 68685, 812), Array(3, 68686, 811), Array(3, 68687, 811), Array(3, 68688, 812),
          Array(3, 68689, 813), Array(4, 68683, 812), Array(4, 68684, 811), Array(4, 68685, 811), Array(4, 68686, 812),
          Array(4, 68687, 813), Array(4, 68688, 813), Array(4, 68689, 813), Array(5, 68683, 811), Array(5, 68684, 812),
          Array(5, 68685, 813), Array(5, 68686, 813), Array(5, 68687, 813), Array(5, 68688, 812), Array(5, 68689, 811),
          Array(6, 68683, 812), Array(6, 68684, 812), Array(6, 68685, 812), Array(6, 68686, 812), Array(6, 68687, 811),
          Array(6, 68688, 810), Array(6, 68689, 811), Array(7, 68683, 812), Array(7, 68684, 812), Array(7, 68685, 811),
          Array(7, 68686, 810), Array(7, 68687, 811), Array(7, 68688, 812), Array(7, 68689, 812), Array(8, 68683, 811),
          Array(8, 68684, 810), Array(8, 68685, 811), Array(8, 68686, 812), Array(8, 68687, 812), Array(8, 68688, 812),
          Array(8, 68689, 812), Array(9, 68683, 811), Array(9, 68684, 812), Array(9, 68685, 812), Array(9, 68686, 812),
          Array(9, 68687, 812), Array(9, 68688, 811), Array(9, 68689, 810), Array(10, 68683, 812), Array(10, 68684, 812),
          Array(10, 68685, 812), Array(10, 68686, 811), Array(10, 68687, 810), Array(10, 68688, 811), Array(10, 68689, 812),
          Array(11, 68683, 812), Array(11, 68684, 811), Array(11, 68685, 810), Array(11, 68686, 811), Array(11, 68687, 812),
          Array(11, 68688, 812), Array(11, 68689, 812))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ =>  throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_(1)).sortBy(_.head)

        r should equal(expected(i))
      }
    })
  }

  it should "run the v2 UI metadata query" in {
    // goes in the event parameerKey query
    runTest(EqlQueriesCan.uiUnityMetadataSourceV2, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected: Array[Any] = Array(
        Array(Array(50)),
        Array(Array(1250)),
        Array(Array(12500)),
        Array(Array(1514797260001L)),
        Array(Array(1514797273740L)),
        Array(Array(555666, 16), Array(666777, 17), Array(888999, 17)),
        Array(Array(232323, 325), Array(454545, 300), Array(676767, 325), Array(898989, 300)),
        Array(Array(111222, 10), Array(333444, 10), Array(555666, 10), Array(777888, 10), Array(888999, 10)),
        Array(Array(12121212, 50), Array(13131313, 50), Array(101010101, 50), Array(1414141414, 50), Array(1515151515, 50), Array(1616161616, 50)),
        Array(Array(-1202440757, 250), Array(-1202440756, 250), Array(-1202440755, 250), Array(-1202440754, 250), Array(-1202440753, 250)),
        Array(Array(9876, 416), Array(54321, 417), Array(54329, 417)),
        Array(Array(983, 250), Array(984, 250), Array(985, 250), Array(986, 250), Array(987, 250)),
        Array(Array(12, 156), Array(13, 157), Array(14, 157), Array(15, 156), Array(16, 156), Array(17, 156),
          Array(18, 156), Array(19, 156)),
        Array(Array(1, 1136), Array(2, 1137), Array(3, 1137), Array(4, 1137), Array(5, 1137), Array(6, 1136),
          Array(7, 1136), Array(8, 1136), Array(9, 1136), Array(10, 1136), Array(11, 1136)),
        Array(Array(22, 16), Array(33, 17), Array(44, 17)),
        Array(Array(-1, 50)),
        Array(Array(82137, 892), Array(82138, 893), Array(82139, 893), Array(82140, 893), Array(82141, 893), Array(82142, 893), Array(82143, 893)),
        Array(Array(1, 68683, 810), Array(1, 68684, 811), Array(1, 68685, 812), Array(1, 68686, 812), Array(1, 68687, 812),
          Array(1, 68688, 812), Array(1, 68689, 811), Array(2, 68683, 812), Array(2, 68684, 813), Array(2, 68685, 813),
          Array(2, 68686, 813), Array(2, 68687, 812), Array(2, 68688, 811), Array(2, 68689, 811), Array(3, 68683, 813),
          Array(3, 68684, 813), Array(3, 68685, 812), Array(3, 68686, 811), Array(3, 68687, 811), Array(3, 68688, 812),
          Array(3, 68689, 813), Array(4, 68683, 812), Array(4, 68684, 811), Array(4, 68685, 811), Array(4, 68686, 812),
          Array(4, 68687, 813), Array(4, 68688, 813), Array(4, 68689, 813), Array(5, 68683, 811), Array(5, 68684, 812),
          Array(5, 68685, 813), Array(5, 68686, 813), Array(5, 68687, 813), Array(5, 68688, 812), Array(5, 68689, 811),
          Array(6, 68683, 812), Array(6, 68684, 812), Array(6, 68685, 812), Array(6, 68686, 812), Array(6, 68687, 811),
          Array(6, 68688, 810), Array(6, 68689, 811), Array(7, 68683, 812), Array(7, 68684, 812), Array(7, 68685, 811),
          Array(7, 68686, 810), Array(7, 68687, 811), Array(7, 68688, 812), Array(7, 68689, 812), Array(8, 68683, 811),
          Array(8, 68684, 810), Array(8, 68685, 811), Array(8, 68686, 812), Array(8, 68687, 812), Array(8, 68688, 812),
          Array(8, 68689, 812), Array(9, 68683, 811), Array(9, 68684, 812), Array(9, 68685, 812), Array(9, 68686, 812),
          Array(9, 68687, 812), Array(9, 68688, 811), Array(9, 68689, 810), Array(10, 68683, 812), Array(10, 68684, 812),
          Array(10, 68685, 812), Array(10, 68686, 811), Array(10, 68687, 810), Array(10, 68688, 811), Array(10, 68689, 812),
          Array(11, 68683, 812), Array(11, 68684, 811), Array(11, 68685, 810), Array(11, 68686, 811), Array(11, 68687, 812),
          Array(11, 68688, 812), Array(11, 68689, 812))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ =>  throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_(1)).sortBy(_.head)

        r should equal(expected(i))
      }
    })
  }

  it should "run the v3 UI metadata query" in {
    // goes in the event parameerKey query
    runTest(EqlQueriesCan.uiUnityMetadataSourceV3, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected: Array[Any] = Array(
        Array(Array(50)),
        Array(Array(1250)),
        Array(Array(12500)),
        Array(Array(1514797260001L)),
        Array(Array(1514797273740L)),
        Array(Array(555666, 16), Array(666777, 17), Array(888999, 17)),
        Array(Array(232323, 325), Array(454545, 300), Array(676767, 325), Array(898989, 300)),
        Array(Array(111222, 10), Array(333444, 10), Array(555666, 10), Array(777888, 10), Array(888999, 10)),
        Array(Array(12121212, 50), Array(13131313, 50), Array(101010101, 50), Array(1414141414, 50), Array(1515151515, 50), Array(1616161616, 50)),
        Array(Array(-1202440757, 250), Array(-1202440756, 250), Array(-1202440755, 250), Array(-1202440754, 250), Array(-1202440753, 250)),
        Array(Array(9876, 416), Array(54321, 417), Array(54329, 417)),
        Array(Array(983, 250), Array(984, 250), Array(985, 250), Array(986, 250), Array(987, 250)),
        Array(Array(12, 156), Array(13, 157), Array(14, 157), Array(15, 156), Array(16, 156), Array(17, 156),
          Array(18, 156), Array(19, 156)),
        Array(Array(1, 1136), Array(2, 1137), Array(3, 1137), Array(4, 1137), Array(5, 1137), Array(6, 1136),
          Array(7, 1136), Array(8, 1136), Array(9, 1136), Array(10, 1136), Array(11, 1136)),
        Array(Array(22, 16), Array(33, 17), Array(44, 17)),
        Array(Array(-1, 50)),
        Array(Array(82137, 892), Array(82138, 893), Array(82139, 893), Array(82140, 893), Array(82141, 893), Array(82142, 893), Array(82143, 893)),
        Array(Array(1, 68683, 810), Array(1, 68684, 811), Array(1, 68685, 812), Array(1, 68686, 812), Array(1, 68687, 812),
          Array(1, 68688, 812), Array(1, 68689, 811), Array(2, 68683, 812), Array(2, 68684, 813), Array(2, 68685, 813),
          Array(2, 68686, 813), Array(2, 68687, 812), Array(2, 68688, 811), Array(2, 68689, 811), Array(3, 68683, 813),
          Array(3, 68684, 813), Array(3, 68685, 812), Array(3, 68686, 811), Array(3, 68687, 811), Array(3, 68688, 812),
          Array(3, 68689, 813), Array(4, 68683, 812), Array(4, 68684, 811), Array(4, 68685, 811), Array(4, 68686, 812),
          Array(4, 68687, 813), Array(4, 68688, 813), Array(4, 68689, 813), Array(5, 68683, 811), Array(5, 68684, 812),
          Array(5, 68685, 813), Array(5, 68686, 813), Array(5, 68687, 813), Array(5, 68688, 812), Array(5, 68689, 811),
          Array(6, 68683, 812), Array(6, 68684, 812), Array(6, 68685, 812), Array(6, 68686, 812), Array(6, 68687, 811),
          Array(6, 68688, 810), Array(6, 68689, 811), Array(7, 68683, 812), Array(7, 68684, 812), Array(7, 68685, 811),
          Array(7, 68686, 810), Array(7, 68687, 811), Array(7, 68688, 812), Array(7, 68689, 812), Array(8, 68683, 811),
          Array(8, 68684, 810), Array(8, 68685, 811), Array(8, 68686, 812), Array(8, 68687, 812), Array(8, 68688, 812),
          Array(8, 68689, 812), Array(9, 68683, 811), Array(9, 68684, 812), Array(9, 68685, 812), Array(9, 68686, 812),
          Array(9, 68687, 812), Array(9, 68688, 811), Array(9, 68689, 810), Array(10, 68683, 812), Array(10, 68684, 812),
          Array(10, 68685, 812), Array(10, 68686, 811), Array(10, 68687, 810), Array(10, 68688, 811), Array(10, 68689, 812),
          Array(11, 68683, 812), Array(11, 68684, 811), Array(11, 68685, 810), Array(11, 68686, 811), Array(11, 68687, 812),
          Array(11, 68688, 812), Array(11, 68689, 812))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ =>  throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_(1)).sortBy(_.head)

        r should equal(expected(i))
      }
    })
  }

  it should "run the v1 UI dimensions query" in {
    // goes in the event parameerKey query
    runTest(EqlQueriesCan.uiUnityDimensionUsageSourceV1, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected: Array[Any] = Array(
        Array(Array(555666, 16), Array(666777, 17), Array(888999, 17)),
        Array(Array(232323, 325), Array(454545, 300), Array(676767, 325), Array(898989, 300)),
        Array(Array(111222, 10), Array(333444, 10), Array(555666, 10), Array(777888, 10), Array(888999, 10)),
        Array(Array(12121212, 50), Array(13131313, 50), Array(101010101, 50), Array(1414141414, 50), Array(1515151515, 50), Array(1616161616, 50)),
        Array(Array(1, 1136), Array(2, 1137), Array(3, 1137), Array(4, 1137), Array(5, 1137), Array(6, 1136),
          Array(7, 1136), Array(8, 1136), Array(9, 1136), Array(10, 1136), Array(11, 1136))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ =>  throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_(1)).sortBy(_.head)

        r should equal(expected(i))
      }
    })
  }

  it should "run the v2 UI dimensions query" in {
    // goes in the event parameerKey query
    runTest(EqlQueriesCan.uiUnityDimensionUsageSourceV2, 100, 200, { result =>
      if (!result.resultStatus.isSuccess)
        throw VitalsException(s"execution failed: ${result.resultStatus}")
      if (result.groupMetrics.executionMetrics.overflowed > 0)
        throw VitalsException(s"execution overflowed")
      if (result.groupMetrics.executionMetrics.limited > 0)
        throw VitalsException(s"execution limited")
      if (result.groupMetrics.executionMetrics.rowCount <= 0)
        throw VitalsException(s"execution row count mismatch: expected rows got ${result.groupMetrics.executionMetrics.rowCount}")

      // all the besides should return a result sert
      result.resultSets.keys.size should be > 0
      val expected: Array[Any] = Array(
        Array(Array(555666, 16), Array(666777, 17), Array(888999, 17)),
        Array(Array(232323, 325), Array(454545, 300), Array(676767, 325), Array(898989, 300)),
        Array(Array(111222, 10), Array(333444, 10), Array(555666, 10), Array(777888, 10), Array(888999, 10)),
        Array(Array(12121212, 50), Array(13131313, 50), Array(101010101, 50), Array(1414141414, 50), Array(1515151515, 50), Array(1616161616, 50)),
        Array(Array(1, 1136), Array(2, 1137), Array(3, 1137), Array(4, 1137), Array(5, 1137), Array(6, 1136),
          Array(7, 1136), Array(8, 1136), Array(9, 1136), Array(10, 1136), Array(11, 1136))
      )
      for (i <- 0 until result.resultSets.size)
      {
        val r = result.resultSets(i).rowSet.map {
          row =>
            row.cells.map {
              cell =>
                cell.bType match {
                  case BrioTypes.BrioLongKey => cell.asLong
                  case BrioTypes.BrioStringKey => cell.asString.hashCode.toLong
                  case _ =>  throw VitalsException("not implemented")
                }
            }
        }.sortBy(_.last).sortBy(_(1)).sortBy(_.head)

        r should equal(expected(i))
      }
    })
  }
}
