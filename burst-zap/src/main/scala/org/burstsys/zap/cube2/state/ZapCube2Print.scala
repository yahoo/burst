/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.state

import org.burstsys.tesla.TeslaTypes
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.state._
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.logging.{burstStdMsg, log}
import org.burstsys.vitals.text.VitalsTextCodec

import scala.collection.mutable

/**
 * helpful user friendly cube dump
 */
trait ZapCube2Print extends Any with ZapCube2State {

  @inline final override
  def bucketStdDeviation: Double = {
    val counts =
      for (bucket <- 0 until bucketsCount) yield
        bucketListLength(bucket).toDouble
    new StandardDeviation(false).evaluate(counts.toArray)
  }

  @inline final override
  def bucketListLengthMax: Int = (for (bucket <- 0 until bucketsCount) yield bucketListLength(bucket)).max

  @inline final private
  def bucketListLength(index: Int): Int = {
    var count = 0
    val headOffset = bucketRead(index)
    if (headOffset ==  EmptyBucket)
      count = 0
    else {
      var currentRow = ZapCube2Row(this, headOffset)
      count += 1
      while (!currentRow.isListEnd) {
        currentRow = ZapCube2Row(this, currentRow.link)
        count += 1
      }
    }
    count
  }

  final override
  def toString: String = {
    val bucketsSize = bucketsCount * TeslaTypes.SizeOfLong
    val cursorSize = byteSize(dimCount)
    val pivotSize = byteSize(dimCount)
    val rowSize = ZapCube2Row.byteSize(dimCount, aggCount)
    s"""Cube2 {
       |
       |  basePtr=$basePtr
       |  current_size=${rowsEnd} byte(s)
       |  availableMemorySize=$availableMemorySize byte(s)
       |
       |  FIXED SIZE SECTION [ start=0, size=$endOfFixedSizeHeader byte(s) ]
       |    poolId=$poolId
       |    dimCount=$dimCount
       |    aggCount=$aggCount
       |    bucketsStart=$bucketsStart
       |    bucketsCount=$bucketsCount
       |    cursorStart=$cursorStart
       |    cursorRow=$cursorRow
       |    cursorUpdated=$cursorUpdated
       |    pivotStart=$pivotStart
       |    rowSize=$rowSize
       |    rowsStart=$rowsStart
       |    rowsEnd=$rowsEnd
       |    rowsCount=$rowsCount
       |    resizeCount=$resizeCount
       |    rowsLimited=$rowsLimited
       |
       | VARIABLE SIZED SECTION [ start=$bucketsStart, size=${bucketsSize + cursorSize} byte(s) ]
       |    BUCKETS [ bucketsStart=$bucketsStart, bucketsSize=$bucketsSize byte(s) ]
       |      ${(for (b <- 0 until bucketsCount) yield s"b$b=${bucketRead(b)}").mkString(", ")}"
       |    CURSOR [ cursorStart=$cursorStart, cursorSize=$cursorSize byte(s) ]
       |      ${cursor.toString}
       |    PIVOT [ cursorStart=$pivotStart, pivotSize=$pivotSize byte(s) ]
       |      ${pivot.toString}
       |
       | RUNTIME SIZED DATA
       |    ROWS [ rowsCount=$rowsCount rowsStart=$rowsStart, rowsEnd=$rowsEnd, size=${rowSize * rowsCount} byte(s) ]
       |}""".stripMargin
  }

  final
  def bucketsPrint: String = {
    val builder = new mutable.StringBuilder
    for (b <- 0 until bucketsCount) {
      builder ++= s"\t\t[b:$b]\n"
      bucketRead(b) match {
        case EmptyBucket =>
        case bucketListHead =>
          var currentOffset = bucketListHead
          while (currentOffset != EmptyLink) {
            val r = ZapCube2Row(this, currentOffset)
            builder ++= s"\t\t\t[off:${rowOffset(r)}] $r\n"
            currentOffset = r.link
          }
      }
    }
    s"""Cube2 {  basePtr=$basePtr
       |  bucketStdDeviation=$bucketStdDeviation
       |  bucketListLengthMax=$bucketListLengthMax
       |
       |  BUCKETS [ bucketsStart=$bucketsStart ]
       |      ${(for (b <- 0 until bucketsCount) yield s"b$b=${bucketRead(b)}").mkString(", ")}"
       |${builder.toString()}
       |}""".stripMargin
  }

  final
  def rowsPrint: String = {
    val builder = new mutable.StringBuilder
    for (b <- 0 until rowsCount) {
      val r = row(b)
      builder ++= s"\t\t\t$b [off:${rowOffset(r)}] $r\n"
    }
    builder.toString()
  }

  @inline final override
  def printCubeState(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, msg: String): Unit = {
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    log warn burstStdMsg(
      s"""|printCubeState $msg
          | keyOverflowed=${thisDictionary.keyOverflowed} slotOverflowed=${thisDictionary.slotOverflowed}"
          | rowCount=${thisCube.itemCount}
          | ${thisCube.printCube(builder, thisCube, thisDictionary)}
          | ${thisDictionary.dump}
          |""".stripMargin
    )
  }

  @inline final override
  def distribution(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): Double = this.bucketStdDeviation

  @inline final override
  def printCube(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): String = this.toString

}
