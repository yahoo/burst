/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.execute.invoke

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.execution.model.execute.parameters.{FabricCall, FabricCallContext}
import org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation.FabricReporter
import org.burstsys.vitals.time.VitalsTimeZones.{BurstDefaultTimeZoneKey, VitalsTimeZoneKey}

/**
  * A once per query execution set of properties. since query code generations
  * can be cached and reused - this is the part that is unique for each potentially
  * cached execution
  */
trait FabricInvocation extends Any {

  /**
    * the ''call'' context for this execution
    *
    * @return
    */
  def call: FabricCall

  /**
    * the source language snippet for this execution
    *
    * @return
    */
  def groupSource: String

  /**
    * default time zone for this execution
    *
    * @return
    */
  def timeZone: VitalsTimeZoneKey

  /**
    * level of reporting within the execution
    *
    * @return
    */
  def reportLevel(level: FabricReportLevel): FabricInvocation

  /**
    * set the troubleshooting reporter
    *
    * @return
    */
  def withReporter(reporter: FabricReporter): FabricInvocation

  /**
    * access the reporter
    * @return
    */
  def reporter: FabricReporter

  /**
    * the ''now'' time for the entire invocation
    * @return
    */
  def now:Long

  /**
    * make a report
    *
    * @param text
    * @param reportLevel
    */
  def report(text: String, reportLevel: FabricReportLevel = FabricDebugLevel): Unit

}

sealed case class FabricReportLevel(level: Int) {
  override def toString: String = {
    getClass.getName.stripSuffix("$")
  }
}

object FabricTacitLevel extends FabricReportLevel(0)

object FabricInfoLevel extends FabricReportLevel(1)

object FabricTraceLevel extends FabricReportLevel(2)

object FabricDebugLevel extends FabricReportLevel(3)

object FabricInvocation {

  type FabricReporter = String => Unit

  def apply(): FabricInvocation =
    FabricInvocationContext(
      call = FabricCall(),
      groupSource = "",
      now = System.currentTimeMillis,
      timeZone = BurstDefaultTimeZoneKey
    )

  def apply(
             call: FabricCall,
             hydraSource: String,
             timeZone: Option[VitalsTimeZoneKey] = None
           ): FabricInvocation =
    FabricInvocationContext(
      call = call: FabricCall,
      groupSource = hydraSource: String,
      now = System.currentTimeMillis,
      timeZone = timeZone.getOrElse(BurstDefaultTimeZoneKey)
    )

}

final case
class FabricInvocationContext(
                               var call: FabricCall,
                               var groupSource: String,
                               var now:Long,
                               var timeZone: VitalsTimeZoneKey
                             ) extends KryoSerializable with FabricInvocation {

  ///////////////////////////////////////////////////////////////////
  // state
  ///////////////////////////////////////////////////////////////////

  override
  def toString: String =
    s"""FabricInvocation(
       |  parameters=$call
       |  groupSource=$groupSource
       |)""".stripMargin

  private[this]
  var _reportLevel: FabricReportLevel = FabricInfoLevel

  private[this]
  var _reporter: FabricReporter = _

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override
  def reportLevel(level: FabricReportLevel): FabricInvocation = {
    _reportLevel = level
    this
  }

  override
  def withReporter(reporter: FabricReporter): FabricInvocation = {
    _reporter = reporter
    this
  }

  override
  def report(text: String, reportLevel: FabricReportLevel): Unit = {
    if (_reporter != null && reportLevel.level >= reportLevel.level) _reporter(text)
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    call = kryo.readClassAndObject(input).asInstanceOf[FabricCallContext]
    groupSource = input.readString
    now = input.readLong
    timeZone = input.readShort
    _reportLevel = kryo.readClassAndObject(input).asInstanceOf[FabricReportLevel]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, call)
    output writeString groupSource
    output writeLong now
    output writeShort timeZone
    kryo.writeClassAndObject(output, _reportLevel)
  }

  override
  def reporter: FabricReporter = _reporter

}
