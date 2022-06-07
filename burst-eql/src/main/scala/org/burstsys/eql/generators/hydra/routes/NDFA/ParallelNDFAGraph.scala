/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

class ParallelNDFAGraph(graphs: NDFAGraph*) extends NDFAGraph {
  {
    for (g <- graphs) {
      enter.addTransition(g.enter)
      g.exit.setTerminating(false).addTransition(exit)
    }
    exit.setTerminating(true)
    this
  }
}
