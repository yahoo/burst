/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext, ZapCubeEmptyBucket}
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation

trait ZapCubePrinter extends Any with ZapCube {

  @inline final override
  def distribution(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): Double = {
    val counts = for (bucket <- 0 until bucketCount) yield
      dumpBucket(builder.asInstanceOf[ZapCubeBuilder], thisCube.asInstanceOf[ZapCubeContext], thisDictionary, bucket)._1.toDouble
    new StandardDeviation(false).evaluate(counts.toArray)
  }

  @inline final override
  def printCube(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): String = {
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    val sb = new StringBuilder
    sb ++= s"\n--------------- CUBE(rowCount=${thisCube.rowCount}) -----------------------------------------------"
    var rowCount = 0
    for (bucket <- 0 until bucketCount) {
      val buckets = dumpBucket(builder.asInstanceOf[ZapCubeBuilder], thisCube.asInstanceOf[ZapCubeContext], thisDictionary, bucket) match {
        case null =>
        case p =>
          rowCount += p._1
          sb ++= p._2
      }
    }
    if (rowCount == 0) s"EMPTY" else s"$sb\n-----------------------------------------------"
  }

  @inline private
  def dumpBucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary, bucketIndex: Int): (Int, String) = {
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    val firstRowOffset = thisCube.bucket(builder, thisCube, bucketIndex)
    if (firstRowOffset == ZapCubeEmptyBucket) {
      null
    } else {
      var ordinal = 0
      var rowCount = 1
      val sb = new StringBuilder
      sb ++= s"\n\t[BUCKET=$bucketIndex]"
      var currentRow = cube.ZapCubeRow(firstRowOffset)
      sb ++= s"\n\t\t[ordinal=$ordinal] " + currentRow.printRow(builder, thisCube, thisDictionary)
      while (currentRow.hasLinkRow(builder, thisCube)) {
        currentRow = currentRow.linkRow(builder, thisCube)
        ordinal += 1
        rowCount += 1
        sb ++= s"\n\t\t[ordinal=$ordinal] " + currentRow.printRow(builder, thisCube, thisDictionary)
      }
      (rowCount, sb.toString())
    }
  }

  @inline final override
  def printCubeState(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, msg: String): Unit = {
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    log warn burstStdMsg(
      s"""|printCubeState $msg
          | keyOverflowed=${thisDictionary.keyOverflowed} slotOverflowed=${thisDictionary.slotOverflowed}"
          | rowCount=${thisCube.rowCount}
          | ${thisCube.printCube(builder, thisCube, thisDictionary)}
          | ${thisDictionary.dump}
          |""".stripMargin
    )
  }

}
