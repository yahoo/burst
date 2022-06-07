/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes

import org.burstsys.eql.generators.hydra.routes.DFA.DFAState.{DFAContext, createContext}
import org.burstsys.eql.generators.hydra.routes.NDFA.{NDFAGraph, NodeSet}

import scala.collection.mutable

package object DFA {

  def toDFA(ndfa:  NDFAGraph): DFAGraph = {
    ndfa.cleanEpsilonTransitions
    implicit val context: DFAContext = createContext

    val startState = DFAState(Set(ndfa.enter))
    val work: mutable.Queue[DFAState] = mutable.Queue(startState)
    val completed: mutable.Set[DFAState] = mutable.Set()
    while(work.nonEmpty) {
      val s = work.dequeue()
      val groupedTransitionMap = s.nodes.flatMap(n => n.transitions).groupBy(_.tag).transform((_, trans) => trans.map(_.node))
      for ((tag, nodeSet) <- groupedTransitionMap) {
        var toDFAState = DFAState(nodeSet)
        toDFAState.terminating = nodeSet.exists(_.terminating)
        val checkIfSeen = if (toDFAState.nodes == s.nodes) {
          Some(s)
        } else {
          val workElement = work.find(_.nodes == toDFAState.nodes)
          val completedElement = completed.find(_.nodes == toDFAState.nodes)
          if (workElement.isDefined)
            workElement
          else
            completedElement
        }
        if (checkIfSeen.isEmpty)
          work.enqueue(toDFAState)
        else
          toDFAState = checkIfSeen.get

        s.transitions.put(tag, toDFAState)
      }
      completed.add(s)
    }
    DFAGraph(startState)
  }

  def expandToNodePerInput(dfa: DFAGraph): DFAGraph = {
    case class AugmentedNodes(stepId: StepTag, nodeSet: NodeSet)
    // reset the state context since we duplicate the old one entirely and want to restart from 1
    implicit val context: DFAContext = createContext
    val startState = DFAState(dfa.startState.nodes) // this one is never mapped
    val work: mutable.Queue[(DFAState, DFAState)] = mutable.Queue((dfa.startState, startState))
    val map: mutable.Map[AugmentedNodes, DFAState] = mutable.Map()
    while(work.nonEmpty) {
      val (oldState, newState) = work.dequeue()
      // for every multiple transition to the same next state we duplicate the next
      for ((stepKey, dfaState) <- oldState.transitions) {
        val stepDFAState = map.getOrElseUpdate(AugmentedNodes(stepKey, dfaState.nodes), {
          val dupState = DFAState(dfaState.nodes)
          work.enqueue((dfaState, dupState))
          dupState
        })
        newState.transitions.put(stepKey, stepDFAState)
      }
      newState.terminating = oldState.terminating
    }
    DFAGraph(startState)
  }
}
