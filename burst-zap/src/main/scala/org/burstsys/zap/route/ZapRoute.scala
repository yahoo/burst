/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.route.FeltRouteCollector
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.route.course.ZapRouteCourseReader
import org.burstsys.zap.route.machine.{ZapRouteMachine, ZapRouteRewriter}
import org.burstsys.zap.route.state.ZapRouteState

/**
 * ==memory layout==
 * We use a fixed size log style memory block for storing entries
 * that record a max of m paths each of a max of n steps each. This is stored
 * in off heap memory and accessed using 'unsafe' memory operations.
 * {{{
 *   |  32b  |  32b    |  64b  |
 *   | path1 | step1  | time0 |
 *   | path1 | step2  | time0 |
 *   | path1 | step3  | time0 |
 *   | path2 | step1  | time0 |
 *   | path2 | step2  | time0 |
 *   | path2 | step3  | time0 |
 *   | path2 | step4  | time0 |
 *   | path2 | step1  | time0 |
 *   | -1    | NA     | NA    | (end of paths)
 * )
 * }}}
 */
trait ZapRoute extends Any with FeltRouteCollector with TeslaBlockPart {

  /**
   * This is for unit tests...
   *
   * @return
   */
  def results: Array[Long]

  /**
   * initialize the route for first use
   *
   * @param id
   * @return
   */
  def initialize(id: TeslaPoolId): ZapRoute

  /**
   * initialize the route for reuse
   *
   * @return
   */
  def reset: ZapRoute

}

final case
class ZapRouteContext(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapRouteState with ZapRoute with ZapRouteMachine with ZapRouteCourseReader
  with ZapRouteRewriter with Comparable[ZapRouteContext] {

  @inline override
  def compareTo(o: ZapRouteContext): Int = blockBasePtr.compareTo(o.blockBasePtr)

  override def currentMemorySize: TeslaMemoryOffset = ??? // TODO

  override def rowCount: TeslaPoolId = 0 // TODO what to do with these?

  override def rowCount_=(count: TeslaPoolId): Unit = {
    // TODO what to do with these?
  }

  override def rowLimited: Boolean = false // TODO what to do with these?

  override def rowLimited_=(s: Boolean): Unit = {
    // TODO what to do with these?
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    // we do not serialize route results from worker to masters yet
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    // we do not serialize route results from worker to masters yet
  }

}
