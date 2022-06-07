/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl.graph

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.felt.model.collectors.route._
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}

/**
 * This is the combination of a possible next step, and the max amount of time allowed to get there from
 * the current step
 */
trait FeltRouteEdge extends Any {

  /**
   * TODO
   *
   * @return
   */
  def stepKey: FeltRouteStepKey

  /**
   * TODO
   *
   * @return
   */
  def maxTime: FeltRouteMaxTime

  /**
   * TODO
   *
   * @return
   */
  def minTime: FeltRouteMaxTime


  def generateCode(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltRouteEdge {

  val edgeClassName: String = classOf[FeltRouteEdge].getName

  def apply(stepKey: FeltRouteStepKey, maxTime: FeltRouteMaxTime, minTime: FeltRouteMaxTime): FeltRouteEdge =
    FeltRouteEdgeContext(stepKey, maxTime, minTime)

  def apply(stepKey: FeltRouteStepKey, maxTime: FeltRouteMaxTime): FeltRouteEdge =
    FeltRouteEdgeContext(stepKey, maxTime, FeltRouteForever)

  def apply(stepKey: FeltRouteStepKey): FeltRouteEdge =
    FeltRouteEdgeContext(stepKey, FeltRouteForever, FeltRouteForever)

  def apply(): FeltRouteEdge = FeltRouteEdgeContext(-1, FeltRouteForever, FeltRouteForever)

}

private
final case
class FeltRouteEdgeContext(var stepKey: FeltRouteStepKey, var maxTime: FeltRouteMaxTime, var minTime: FeltRouteMaxTime)
  extends AnyRef with KryoSerializable with FeltRouteEdge {

  import FeltRouteEdge._

  override
  def toString: String = s"edge(key=$stepKey, maxTime=$maxTime, minTime=$minTime)"

  ///////////////////////////////////////////////////////////////////////////////////
  // CODE GEN
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""
       |$I$edgeClassName($stepKey, ${maxTime}L, ${minTime}L)""".stripMargin
  }

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeInt stepKey
    output writeLong minTime
    output writeLong maxTime
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    stepKey = input.readInt
    minTime = input.readLong
    maxTime = input.readLong
  }
}

