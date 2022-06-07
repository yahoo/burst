/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning

import java.util.concurrent.atomic.AtomicLong

package object lanes {
  private val currentIndex: AtomicLong = new AtomicLong(1)
  abstract class LaneName(val name: String) {
    val ordinal: Long = currentIndex.incrementAndGet()

    override def toString = s"Lane[$ordinal]($name)"

    private[planning] def createLane(): LaneActions = new LaneActions()
  }

  sealed case class BasicLaneName(override val name: String) extends LaneName(name) {
    override def toString = s"BasicLane[$ordinal]($name)"
  }

  sealed case class DependentLaneName(override val name: String) extends LaneName(name) {
    override def toString = s"DependentLane[$ordinal]($name)"
  }

  val CLEANUP: LaneName = BasicLaneName("CLEANUP")
  val RESULT: LaneName = BasicLaneName("RESULT")
  val INIT_PREFIX: String = "INIT"
  val INIT: LaneName = new LaneName(s"$INIT_PREFIX-RESULT") {
    override val ordinal: Long = Long.MaxValue
  }
}
