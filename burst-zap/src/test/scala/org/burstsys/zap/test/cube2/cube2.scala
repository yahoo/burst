/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test

import org.burstsys.brio.dictionary
import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.tesla.thread.request.TeslaRequestCoupler
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.logging._
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}
import org.burstsys.zap.cube2.flex.ZapFlexCube2
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

package object cube2 {

  trait ZapCube2Spec extends AnyFlatSpec with Matchers {

    implicit val text: VitalsTextCodec = VitalsTextCodec()

    VitalsLog.configureLogging("zap", consoleOnly = true)

    def builder: ZapCube2Builder

    var dictA: BrioFlexDictionary = _
    var cubeA: ZapCube2 = _
    var dictB: BrioFlexDictionary = _
    var cubeB: ZapCube2 = _
    var dictC: BrioFlexDictionary = _
    var cubeC: ZapCube2 = _

    def grab(): Unit = {
      dictA = dictionary.flex.grabFlexDictionary()
      cubeA = zap.cube2.flex.grabFlexCube(dictA, builder = builder)
      dictB = dictionary.flex.grabFlexDictionary()
      cubeB = zap.cube2.flex.grabFlexCube(dictB, builder = builder)
      dictC = dictionary.flex.grabFlexDictionary()
      cubeC = zap.cube2.flex.grabFlexCube(dictC, builder = builder)
    }

    def release(): Unit = {
      zap.cube2.flex.releaseFlexCube(cubeA)
      dictionary.flex.releaseFlexDictionary(dictA)
      zap.cube2.flex.releaseFlexCube(cubeB)
      dictionary.flex.releaseFlexDictionary(dictB)
      zap.cube2.flex.releaseFlexCube(cubeC)
      dictionary.flex.releaseFlexDictionary(dictC)
    }

    def defineExplicitLong(cube: ZapCube2, rows: Tuple4[Long, Long, Long, Long]*): Unit = {
      rows foreach {
        r =>
          cube.dimWrite(0, r._1)
          cube.dimWrite(1, r._2)
          cube.aggWrite(0, r._3)
          cube.aggWrite(1, r._4)
      }
    }

    def defineExplicitString(cube: ZapCube2, rows: Tuple4[String, String, String, String]*): Unit = {
      rows foreach {
        r =>
          val d = cube.dictionary
          cube.dimWrite(0, d.keyLookupWithAdd(r._1))
          cube.dimWrite(1, d.keyLookupWithAdd(r._2))
          cube.aggWrite(0, d.keyLookupWithAdd(r._3))
          cube.aggWrite(1, d.keyLookupWithAdd(r._4))
      }
    }

    def defineAscending(cube: ZapCube2, rowCount: Int, startIndex: Int = 0): Unit = {
      var i = 0
      for (i <- startIndex until rowCount + startIndex) {
        cube.dimWrite(0, i)
        cube.dimWrite(1, i)
        cube.aggWrite(0, i)
        cube.aggWrite(1, i)
      }
    }

    def defineRandom(cube: ZapCube2, rowCount: Int): Unit = {
      var i = 0
      while (i < rowCount) {
        cube.dimWrite(0, randomPositiveLong)
        cube.dimWrite(1, randomPositiveLong)
        cube.aggWrite(0, randomPositiveLong)
        cube.aggWrite(1, randomPositiveLong)
        i += 1
      }
    }

    private def randomPositiveLong = {
      math.abs(Random.nextLong())
    }

    final
    def assertAscendingLong(cube: ZapCube2, rowCount: Int, startIndex: Int = 0): Unit = {
      val errors = new ArrayBuffer[String]()
      if (cube.rowsCount != rowCount) errors += s"cube.rowsCount(${cube.rowsCount}) != rowCount($rowCount)"
      val rows = rowSet(cube)
      var i: Long = 0
      while (i < cube.rowsCount) {
        val r = cube.row(i.toInt)
        val tuple = (i, i, i, i)
        val test = rows.contains(tuple)
        if (!test)
          errors += s"row $tuple not found"
        i += 1
      }
      if (errors.nonEmpty)
        throw new TestFailedException(
          s"""|---------------------------------------------------------
              |FAIL! ${errors.mkString("{\n\t", ",\n\t", "\n}")}
              |---------------------------------------------------------
              |ACTUAL:
              |${printLongRows(cube)}
              |---------------------------------------------------------
              |${cube.toString}""".stripMargin,
          1)
    }

    final
    def assertTestLong(cube: ZapCube2, rowCount: Int, body: Tuple4[Long, Long, Long, Long] => Tuple4[Long, Long, Long, Long]): Unit = {
      val errors = new ArrayBuffer[String]()
      if (cube.rowsCount != rowCount) errors += s"cube.rowsCount(${cube.rowsCount}) != rowCount($rowCount)"
      val rows = rowSet(cube)
      var i: Long = 0
      while (i < cube.rowsCount) {
        val r = cube.row(i.toInt)
        val tuple = (r.dimRead(0), r.dimRead(1), r.aggRead(0), r.aggRead(1))
        val test = body(tuple)
        if (test != tuple)
          errors += s"row $i $tuple should be $test "
        i += 1
      }
      if (errors.nonEmpty)
        throw new TestFailedException(
          s"""|---------------------------------------------------------
              |FAIL! ${errors.mkString("{\n\t", ",\n\t", "\n}")}
              |---------------------------------------------------------
              |ACTUAL:
              |${printLongRows(cube)}
              |---------------------------------------------------------
              |${cube.toString}""".stripMargin,
          1)
    }

    final
    def assertExplicitLong(cube: ZapCube2, rows: Tuple4[Long, Long, Long, Long]*): Unit = {
      try {
        cube.rowsCount should equal(rows.size)
        var i = 0
        while (i < cube.rowsCount) {
          val r = cube.row(i)
          val tuple = (r.dimRead(0), r.dimRead(1), r.aggRead(0), r.aggRead(1))
          assert(rows.contains(tuple))
          i += 1
        }
      } catch {
        case t: TestFailedException =>
          throw new TestFailedException(
            s"""|---------------------------------------------------------
                |FAIL! ${t.getMessage()}
                |---------------------------------------------------------
                |ACTUAL:
                |${printLongRows(cube)}
                |---------------------------------------------------------
                |${cube.toString}""".stripMargin,
            1)
      }
    }

    final
    def assertExplicitString(cube: ZapCube2, rows: Tuple4[String, String, String, String]*): Unit = {
      try {
        cube.rowsCount should equal(rows.size)
        val d = cube.dictionary
        var i = 0
        while (i < cube.rowsCount) {
          val r = cube.row(i)
          val tuple = (
            d.stringLookup(r.dimRead(0).toShort),
            d.stringLookup(r.dimRead(1).toShort),
            d.stringLookup(r.aggRead(0).toShort),
            d.stringLookup(r.aggRead(1).toShort)
          )
          assert(rows.contains(tuple))
          i += 1
        }
      } catch {
        case t: TestFailedException =>
          throw new TestFailedException(
            s"""|---------------------------------------------------------
                |FAIL! ${t.getMessage()}
                |---------------------------------------------------------
                |ACTUAL:
                |${printStringRows(cube)}
                |---------------------------------------------------------
                |${cube.toString}""".stripMargin,
            1)
      }
    }

    final
    def rowSet(cube: ZapCube2): Array[(Long, Long, Long, Long)] = {
      (for (i <- 0 until cube.rowsCount) yield {
        val r = cube.row(i)
        (r.dimRead(0), r.dimRead(1), r.aggRead(0), r.aggRead(1))
      }).toArray
    }

    final
    def printLongRows(cube: ZapCube2): String = {
      val buffer = new ArrayBuffer[String]
      val rows = for (i <- 0 until cube.rowsCount) yield {
        val r = cube.row(i)
        (r.dimRead(0), r.dimRead(1), r.aggRead(0), r.aggRead(1))
      }
      val hdr = s"\t// (d0, d1, a0, a1)"
      val print = rows.sortBy(_._1).sortBy(_._2).map {
        case (d0, d1, a0, a1) => s"($d0, $d1, $a0, $a1)"
      }
      print.mkString(s"$hdr\n\t", ",\n\t", "")
    }

    final
    def printStringRows(cube: ZapCube2): String = {
      val d = cube.dictionary
      val buffer = new ArrayBuffer[String]
      val rows = for (i <- 0 until cube.rowsCount) yield {
        val r = cube.row(i)
        (
          d.stringLookup(r.dimRead(0).toShort),
          d.stringLookup(r.dimRead(1).toShort),
          d.stringLookup(r.aggRead(0).toShort),
          d.stringLookup(r.aggRead(1).toShort))
      }
      val hdr = s"\t// (d0, d1, a0, a1)"
      val print = rows.sortBy(_._1).sortBy(_._2).sortBy(_._3).sortBy(_._4).map {
        case (d0, d1, a0, a1) => s"($d0, $d1, $a0, $a1)"
      }
      print.mkString(s"$hdr\n\t", ",\n\t", "")
    }

    object CubeTest {
      final
      def apply(body: => Unit): Unit = {
        try {
          TeslaWorkerCoupler {
            grab()
            try {
              body
            } finally release()
          }
        } catch {
          case t: Throwable =>
            log error t.getMessage
            throw t
        }
      }
    }

  }


}
