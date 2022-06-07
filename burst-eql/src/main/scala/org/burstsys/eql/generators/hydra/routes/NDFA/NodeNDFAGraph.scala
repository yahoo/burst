/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes.StepTag

class NodeNDFAGraph(tag: StepTag, capturing: Boolean=true) extends NDFAGraph {
  enter.addTransition(new NDFAState(tag, capturing).addTransition(exit))
}
