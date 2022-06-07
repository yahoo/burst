/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.host

import org.burstsys.vitals.reporter._
import org.burstsys.vitals.reporter.metric.VitalsReporterByteValueMetric

import java.lang.management.{BufferPoolMXBean, ManagementFactory}
import javax.management.{MBeanServer, ObjectName}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.postfixOps

/**
 * helper types/functions for Host/Process/Os state
 */
private[vitals]
object VitalsNativeMemoryReporter extends VitalsReporter {

  final val dName: String = "vitals-native-mem"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final val _nioBufferTotalMemory = VitalsReporterByteValueMetric("nio_direct_buffer_total_mem")

  private[this]
  final val _nioBufferUsedMemory = VitalsReporterByteValueMetric("nio_direct_buffer_used_mem")

  private[this]
  final val _nmtMemory: mutable.Map[String, VitalsReporterByteValueMetric] = mutable.LinkedHashMap()

  private val scale = "KB"
  private val linScale = 1000

  this += _nioBufferUsedMemory
  this += _nioBufferUsedMemory

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def sample(sampleMs: Long): Unit = {
    newSample()

    nativeSample()
    buffersSample()

    super.sample(sampleMs)

  }

  def buffersSample(): Unit = {
    ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.find(_.getName.contains("direct")) match {
      case Some(p) =>
        _nioBufferTotalMemory.record(p.getTotalCapacity)
        _nioBufferUsedMemory.record(p.getMemoryUsed)
      case None =>
    }
  }

  val server:MBeanServer = ManagementFactory.getPlatformMBeanServer
  val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head
  val objectName: ObjectName = new ObjectName("com.sun.management:type=DiagnosticCommand")

  private val totalMatch = "Total: (.*)".r
  private val summaryMatch = "^-\\s*((?:\\w|\\s)*) \\((.*)\\)".r
  // must match `scale` above
  private val valueMatch = "(\\w*)\\s*=\\s*(\\w*)KB".r
  def nativeSample(): Unit = {

    val level = "summary"
    val result =
    try {
      val rtrn = server.invoke(objectName, "vmNativeMemory",
        Array[Object](Array[String](s"$level scale=$scale")),
        Array[String](classOf[Array[String]].getName))
      rtrn.toString.split(System.lineSeparator())
    } catch {
      case e: Exception =>
        log warn s"unable to invoke native memory diagnostic ${e.getMessage}"
        return
    }
    val data = result.flatMap {
      case totalMatch(vals) =>
        Some(valueMatch.findAllIn(vals).matchData.map(m => s"nmt_total_${m.group(1)}" -> m.group(2).toLong * linScale).toList)
      case summaryMatch(summaryName, vals) =>
        Some(valueMatch.findAllIn(vals).matchData.map { m =>
          s"nmt_${summaryName.replace(' ', '_').toLowerCase()}_${m.group(1)}" -> m.group(2).toLong * linScale
        }.toList)
      case _ =>
        None
    }.flatten.toMap
    for (d <- data) {
      _nmtMemory.getOrElseUpdate(d._1, {
        val m = VitalsReporterByteValueMetric(d._1)
        this += m
        m
      }).record(d._2)
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // REPORT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def report: String = {
    if (nullData) return ""
    f"${_nioBufferUsedMemory.report}${_nioBufferTotalMemory.report}${_nmtMemory.keys.toSeq.sorted.map(_nmtMemory(_).report).mkString(" ")}"
  }
}
