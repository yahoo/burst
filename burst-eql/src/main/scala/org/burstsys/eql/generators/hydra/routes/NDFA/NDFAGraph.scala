/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes.Epsilon

import scala.collection.mutable

class NDFAGraph(val enter: NDFANode = new NDFAState(Epsilon), val exit: NDFANode = new NDFAState(Epsilon).setTerminating(true)) {
  def cleanEpsilonTransitions: this.type = {
    // all states
    val states = enter.transitiveStates

    for (s <- states + enter) {
      s.cleanEpsilonTransitions()
    }
    this
  }

  override def clone(): NDFAGraph = {
    val clone = new NDFAGraph(new NDFAState(this.enter.id, this.enter.isCapturing), new NDFAState(this.exit.id, this.exit.isCapturing))
    val cloneMap: mutable.Map[NDFANode, NDFANode] = mutable.Map((this.enter, clone.enter), (this.exit, clone.exit))
    val cloneWork: mutable.Queue[(NDFANode, NDFANode)] = mutable.Queue((this.enter, clone.enter), (this.exit, clone.exit))
    while (cloneWork.nonEmpty) {
      val (originalNode, clonedNode) = cloneWork.dequeue()
      originalNode.transitions.map{t =>
        var mappedNode = cloneMap.getOrElseUpdate(t.node, {
          val newClonedNode = new NDFAState(t.tag, t.node.isCapturing)
          cloneWork.enqueue((t.node, newClonedNode))
          newClonedNode
        })
        clonedNode.addTransition(mappedNode)
      }
    }
    clone
  }

  override def toString: String = {
    val processedSet: mutable.Set[NDFANode] = mutable.Set(this.enter)
    val printWork: mutable.Queue[NDFANode] = mutable.Queue(this.enter)
    val printBuffer: StringBuffer = new StringBuffer()
    while (printWork.nonEmpty) {
      val printNode = printWork.dequeue()
      if (printNode == this.enter)
        printBuffer.append("[S]")
      printBuffer.append(printNode.toString)
      printBuffer.append('\n')
      for (t <- printNode.transitions) {
        if (!processedSet.contains(t.node)) {
          processedSet.add(t.node)
          printWork.enqueue(t.node)
        }
      }
    }
    printBuffer.toString
  }
}

