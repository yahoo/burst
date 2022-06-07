/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes.NDFA.RepeatingNDFAGraph.UNLIMITED
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionRepeat

class RepeatingNDFAGraph(graph: NDFAGraph, minRepititions: Int, maxRepititions: Int) extends NDFAGraph {
  {
    graph.exit.setTerminating(false)
    // we will eventually wrap the final graph
    val finalGraph = new NDFAGraph()
    finalGraph.exit.addTransition(exit)
    finalGraph.exit.setTerminating(false)

    val firstRepeatingGraph = graph.clone()
    var lastAdditionalGraph = firstRepeatingGraph

    assert(minRepititions >= 0)
    assert(maxRepititions == UNLIMITED || minRepititions <= maxRepititions)
    assert(maxRepititions == UNLIMITED || maxRepititions > 0)
    if (minRepititions == 0) {
      finalGraph.enter.addTransition(finalGraph.exit)
    } else {
      assert (minRepititions < 10)
      // need to copy the target graph <min> times for repetition
      for (i <- 1 until minRepititions) {
        val newAdditionalGraph = graph.clone()
        lastAdditionalGraph.exit.addTransition(newAdditionalGraph.enter)
        lastAdditionalGraph = newAdditionalGraph
      }
    }

    if (maxRepititions == UNLIMITED)
      finalGraph.exit.addTransition(lastAdditionalGraph.enter)
    else if (maxRepititions > 1) {
      // need to copy the target graph <max> times for repetition with exits back to the current end
      assert(maxRepititions <= 15)
      for (i <- minRepititions.max(2) to maxRepititions) {
        val newAdditionalGraph = graph.clone()
        lastAdditionalGraph.exit.addTransition(newAdditionalGraph.enter)
        lastAdditionalGraph.exit.addTransition(finalGraph.exit) //optional
        lastAdditionalGraph = newAdditionalGraph
      }
    }

    finalGraph.enter.addTransition(firstRepeatingGraph.enter)
    lastAdditionalGraph.exit.addTransition(finalGraph.exit)

    // wire up to final wrapped graph
    enter.addTransition(finalGraph.enter)
    exit.setTerminating(true)
    this
  }
}

object RepeatingNDFAGraph {
  final val UNLIMITED = FunnelMatchDefinitionRepeat.UNLIMITED
}
