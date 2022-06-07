/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.DFA

import org.burstsys.eql.generators.hydra.routes.DFA.DFAState.DFAContext
import org.burstsys.eql.generators.hydra.routes.NDFA.NodeSet
import org.burstsys.eql.generators.hydra.routes.{StateId, StepTag}

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

object DFAState {
  def createContext: DFAContext = new DFAContext
  class DFAContext {
    def getId: StateId = id.getAndIncrement()
    private val id: AtomicLong = new AtomicLong(0)
  }
}

case class DFAState(nodes:  NodeSet)(implicit context: DFAContext) {
  var terminating: Boolean = false
  val id: StateId = context.getId
  val tacit: Boolean = {
    val captureNodes = nodes.groupBy(_.isCapturing)
    assert(captureNodes.size < 2)
    captureNodes.contains(false)
  }
  val transitions: mutable.Map[StepTag, DFAState] = mutable.Map()

  // check that non-capture and capture NDFA nodes are not mixed

  override def toString: String = {
    val transitionStrings = transitions.map{s => s"${s._1}->${s._2.id}"}.mkString("(",",",")")
    val nodeString = nodes.map{n => s"${n.hashCode()}"}.mkString("[", ",", "]")
    s"$id${if (terminating) "T" else ""}$nodeString$transitionStrings"
  }
}

