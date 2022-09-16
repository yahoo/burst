/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioCourse._
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.collectors.route.decl.graph.FeltRouteTransition
import org.burstsys.felt.model.collectors.route.runtime.FeltRouteDefaultSize
import org.burstsys.felt.model.collectors.route.{FeltRouteBuilder, FeltRouteBuilderContext, FeltRouteStepKey}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}


trait ZapRouteBuilder extends FeltRouteBuilder

object ZapRouteBuilder {

  def apply(maxPartialPaths: Int, maxCompletePaths: Int, maxStepsPerGraph: Int, maxPathTime: Long,
            minCourse: Int, maxCourse: Int,
            entranceSteps: Array[FeltRouteStepKey], exitSteps: Array[FeltRouteStepKey],
            beginSteps: Array[FeltRouteStepKey], tacitSteps: Array[FeltRouteStepKey],
            emitCodes: Array[Int], endSteps: Array[FeltRouteStepKey], completeSteps: Array[FeltRouteStepKey],
            transitions: Array[FeltRouteTransition]): ZapRouteBuilder =

    ZapRouteBuilderContext(
      maxPartialPaths = maxPartialPaths, maxCompletePaths = maxCompletePaths,
      maxStepsPerGraph = maxStepsPerGraph, maxPathTime: Long,
      minCourse = minCourse, maxCourse = maxCourse, entranceSteps = entranceSteps,
      exitSteps = exitSteps, beginSteps = beginSteps,
      tacitSteps = tacitSteps, emitCodes = emitCodes, endSteps = endSteps, completeSteps = completeSteps,
      transitions = transitions: Array[FeltRouteTransition]
    )

  def apply(): ZapRouteBuilder = ZapRouteBuilderContext()

}

private
final case
class ZapRouteBuilderContext(
                              var maxPartialPaths: Int = -1,
                              var maxCompletePaths: Int = -1,
                              var maxStepsPerGraph: Int = -1,
                              var maxPathTime: Long = -1,
                              var minCourse: Int = -1,
                              var maxCourse: Int = -1,
                              var entranceSteps: Array[FeltRouteStepKey] = Array.empty,
                              var exitSteps: Array[FeltRouteStepKey] = Array.empty,
                              var beginSteps: Array[FeltRouteStepKey] = Array.empty,
                              var tacitSteps: Array[FeltRouteStepKey] = Array.empty,
                              var emitCodes: Array[Int] = Array.empty,
                              var endSteps: Array[FeltRouteStepKey] = Array.empty,
                              var completeSteps: Array[FeltRouteStepKey] = Array.empty,
                              var transitions: Array[FeltRouteTransition] = Array.empty
                            ) extends FeltRouteBuilderContext with ZapRouteBuilder {
  override
  def toString: String = {
    val transString = transitions.indices.map {
      i =>
        val t = transitions(i)
        if (t == null) s"WHEN key=$i THEN NONE"
        else s"WHEN key=$i THEN ${t.toString}"
    }.mkString("\n\t", "\n\t", "")
    s"""ZapRouteBuilder(
       | frameId=$frameId
       | frameName=$frameName
       | maxPartialPaths=$maxPartialPaths
       | maxCompletePaths=$maxCompletePaths
       | maxStepsPerGraph=$maxStepsPerGraph
       | maxPathTime=$maxPathTime
       | minCourse=$minCourse
       | maxCourse=$maxCourse
       | entranceSteps=${entranceSteps.mkString}
       | exitSteps=${exitSteps.mkString}
       | beginSteps=${beginSteps.mkString}
       | tacitSteps=${tacitSteps.mkString}
       | emitCodes=${emitCodes.mkString}
       | endSteps=${endSteps.mkString}
       | autoSteps=${completeSteps.mkString}
       | transitions=$transString
       |)""".stripMargin
  }

  @inline
  def noTransitions: Boolean = transitions.isEmpty

  @inline
  def requiredMemorySize: TeslaMemoryOffset = FeltRouteDefaultSize //ZapRouteHeaderSize +
  // ((maxStepsPerPath * ZapRouteJournalEntrySize) * maxPathsPerRoute) + ZapRouteJournalEntrySize

  override def defaultStartSize: TeslaMemorySize = ZapRouteDefaultStartSize

  override
  def init(
            frameId: Int,
            frameName: String,
            binding: FeltBinding,
            maxPartialPaths: Int,
            maxCompletePaths: Int,
            maxStepsPerGraph: Int,
            maxPathTime: Long,
            minCourse: Int, maxCourse: Int,
            entranceSteps: Array[FeltRouteStepKey],
            exitSteps: Array[FeltRouteStepKey],
            beginSteps: Array[FeltRouteStepKey],
            tacitSteps: Array[FeltRouteStepKey],
            emitCodes: Array[Int],
            endSteps: Array[FeltRouteStepKey],
            completeSteps: Array[FeltRouteStepKey],
            transitions: Array[FeltRouteTransition]
          ): Unit = {
    super.init(frameId, frameName, binding)
    this.maxPartialPaths = maxPartialPaths
    this.maxCompletePaths = maxCompletePaths
    this.maxStepsPerGraph = maxStepsPerGraph
    this.maxPathTime = maxPathTime
    this.minCourse = minCourse
    this.maxCourse = maxCourse
    this.entranceSteps = entranceSteps
    this.exitSteps = exitSteps
    this.beginSteps = beginSteps
    this.tacitSteps = tacitSteps
    this.emitCodes = emitCodes
    this.endSteps = endSteps
    this.completeSteps = completeSteps
    this.transitions = transitions
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // Entrance/Exit Steps
  ///////////////////////////////////////////////////////////////////////////////////

  @inline override
  def isEntranceStep(step: FeltRouteStepKey): Boolean = entranceSteps.contains(step)

  @inline override
  def isExitStep(step: FeltRouteStepKey): Boolean = exitSteps.contains(step)

  @inline override
  def isCompleteStep(step: FeltRouteStepKey): Boolean = completeSteps.contains(step)

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output writeInt maxPartialPaths
    output writeInt maxCompletePaths
    output writeInt maxStepsPerGraph
    output writeLong maxPathTime
    output writeInt minCourse
    output writeInt maxCourse

    kryo.writeClassAndObject(output, entranceSteps)
    kryo.writeClassAndObject(output, exitSteps)
    kryo.writeClassAndObject(output, beginSteps)
    kryo.writeClassAndObject(output, tacitSteps)
    kryo.writeClassAndObject(output, emitCodes)
    kryo.writeClassAndObject(output, endSteps)

    output writeInt transitions.length
    var i = 0
    while (i < transitions.length) {
      val d = transitions(i)
      if (d == null) {
        output writeBoolean false
      } else {
        output writeBoolean true
        d.asInstanceOf[KryoSerializable].write(kryo, output)
      }
      i += 1
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    maxPartialPaths = input.readInt
    maxCompletePaths = input.readInt
    maxStepsPerGraph = input.readInt
    maxPathTime = input.readLong
    minCourse = input.readInt
    maxCourse = input.readInt

    entranceSteps = kryo.readClassAndObject(input).asInstanceOf[Array[FeltRouteStepKey]]
    exitSteps = kryo.readClassAndObject(input).asInstanceOf[Array[FeltRouteStepKey]]
    beginSteps = kryo.readClassAndObject(input).asInstanceOf[Array[FeltRouteStepKey]]
    tacitSteps = kryo.readClassAndObject(input).asInstanceOf[Array[FeltRouteStepKey]]
    emitCodes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioCourseId]]
    endSteps = kryo.readClassAndObject(input).asInstanceOf[Array[FeltRouteStepKey]]

    val length = input.readInt
    transitions = new Array[FeltRouteTransition](length)
    var i = 0
    while (i < length) {
      if (input.readBoolean) {
        transitions(i) = FeltRouteTransition()
        transitions(i).asInstanceOf[KryoSerializable].read(kryo, input)
      }
      i += 1
    }
  }

}
