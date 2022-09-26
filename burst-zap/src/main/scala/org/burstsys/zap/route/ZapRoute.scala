/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.route.FeltRouteCollector
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}
import org.burstsys.zap.route.course.ZapRouteCourseReader
import org.burstsys.zap.route.machine.{ZapRouteMachine, ZapRouteRewriter}
import org.burstsys.zap.route.state.{ZapRouteHeaderSize, ZapRouteJournalEntrySize, ZapRouteState}

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
trait ZapRoute extends Any with FeltRouteCollector with TeslaBlockPart with TeslaFlexCollector[ZapRouteBuilder, ZapRoute] {

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

  def printEntries: String
}

final case
class ZapRouteContext(blockPtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal
  with ZapRouteState with ZapRoute with ZapRouteMachine with ZapRouteCourseReader
  with ZapRouteRewriter with Comparable[ZapRouteContext] {

  @inline override
  def compareTo(o: ZapRouteContext): Int =
    blockBasePtr.compareTo(o.blockBasePtr)

  override def currentMemorySize: TeslaMemoryOffset = {
    // get the header and all the entries including the dirty
    ZapRouteHeaderSize + dirtyCursor + ZapRouteJournalEntrySize
  }

  override def itemCount: Int = {
    // the dirty cursor offset 0 from end of header
    commitCursor/ZapRouteJournalEntrySize + 1
  }

  override def itemCount_=(count: TeslaPoolId): Unit = ???

  override def itemLimited: Boolean = {
    routeLimited
  }

  override def itemLimited_=(s: Boolean): Unit = {
    routeLimited = s
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    // we do not serialize route results from worker to supervisors yet
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    // we do not serialize route results from worker to supervisors yet
  }


  override def initialize(pId: TeslaPoolId, builder: ZapRouteBuilder): Unit =
    initialize(pId)

  override def defaultBuilder: ZapRouteBuilder =
    ZapRouteBuilderContext()

  override def builder: ZapRouteBuilder =
    ZapRouteBuilder()
}
