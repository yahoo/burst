/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

class SequentialNDFAGraph(graphs: NDFAGraph*) extends NDFAGraph {
  {
    var prev = enter
    for (g <- graphs) {
      prev.setTerminating(false)
      prev.addTransition(g.enter)
      prev = g.exit
    }
    prev.addTransition(exit)
    prev.setTerminating(false)
    exit.setTerminating(true)
    this
  }
}
