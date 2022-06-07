/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.DFA

import scala.collection.mutable

case class DFAGraph(startState: DFAState) {
  def walkStates(op: DFAState => Unit): Unit = {
    val processedSet: mutable.Set[Long] = mutable.Set(startState.id)
    val opWork: mutable.Queue[DFAState] = mutable.Queue(startState)
    while (opWork.nonEmpty) {
      val printNode = opWork.dequeue()
      op(printNode)
      for (t <- printNode.transitions) {
        if (!processedSet.contains(t._2.id)) {
          processedSet.add(t._2.id)
          opWork.enqueue(t._2)
        }
      }
    }
  }

  override def toString: String = {
    val printBuffer: StringBuffer = new StringBuffer()
    walkStates(s => printBuffer.append(s.toString).append('\n'))
    printBuffer.toString
  }
}
