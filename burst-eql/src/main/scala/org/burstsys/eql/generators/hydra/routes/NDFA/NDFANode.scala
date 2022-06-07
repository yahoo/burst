/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes
import org.burstsys.eql.generators.hydra.routes.StateId

import scala.collection.mutable

trait NDFANode {
  def id: StateId

  def transitions: mutable.ListBuffer[NDFATransition]

  def addTransition(state: NDFANode): NDFANode

  def addTransitions(states: NDFANode*): NDFANode

  def transitiveStates: Set[NDFANode]

  def cleanEpsilonTransitions(): NDFANode

  def terminating: Boolean

  def setTerminating(state: Boolean): NDFANode

  def isCapturing: Boolean

}
