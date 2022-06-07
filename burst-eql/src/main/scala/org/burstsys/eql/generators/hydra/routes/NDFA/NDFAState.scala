/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes.Epsilon
import org.burstsys.eql.generators.hydra.routes.StepTag

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private class NDFAState(val id: StepTag, val capturing: Boolean = true) extends NDFANode {
  val transitions: ListBuffer[NDFATransition] = ListBuffer()

  var terminating: Boolean = false

  def setTerminating(state: Boolean): NDFANode = {
    terminating = state; this
  }

  def addTransition(tag: StepTag, state: NDFANode): NDFANode = {
    transitions += NDFATransition(tag, state)
    this
  }
  def addTransition(state: NDFANode): NDFANode = {
    transitions += NDFATransition(state.id, state)
    this
  }

  def addTransitions(states: NDFANode *): NDFAState = {
    for (s <- states) addTransition(s)
    this
  }

  def transitiveStates: Set[NDFANode] = {
    val rez: mutable.Set[NDFANode] = mutable.Set()
    def finder(current: NDFANode): mutable.Set[NDFANode] = {
      current.transitions.foldLeft(rez){
        (results, next) =>
          if (results.contains(next.node))
            results
          else {
            rez.add(next.node)
            finder(next.node)
          }
      }
    }
    finder(this).toSet
  }

  def findEpsilonSet: Set[NDFANode] = {
    def findTags(state: NDFANode, seen: mutable.Set[NDFANode]): Unit = {
      state.transitions.filter(t => t.tag == Epsilon && !seen.contains(t.node)).foreach{ t =>
        seen.add(t.node)
        findTags(t.node, seen)
      }
    }
    val set = mutable.Set[NDFANode]()
    findTags(this, set)
    set.toSet
  }

  def cleanEpsilonTransitions(): NDFANode = {
    val epsilonSets = findEpsilonSet
    for (es <- epsilonSets) {
      for (et <- es.transitions.filter(t => t.tag != Epsilon))
        addTransition(et.tag, et.node)
    }
    transitions --= transitions.filter(t => t.tag == Epsilon)
    setTerminating(terminating || epsilonSets.exists(_.terminating))
    this
  }

  override def clone(): NDFAState = {
    val newState =  new NDFAState(this.id, this.capturing)
    newState.setTerminating(this.terminating)
    newState.transitions ++= this.transitions
    newState
  }

  override def toString: String = {
    val transitionStrings = transitions.map{s => s"$s"}.mkString("(",",",")")
    s"${if (!isCapturing) "^" else ""}${if (terminating) "[T]" else ""}${this.hashCode()}$transitionStrings"
  }

  def isCapturing: Boolean = capturing
}

