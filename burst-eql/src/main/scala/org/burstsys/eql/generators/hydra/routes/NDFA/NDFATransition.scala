/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes.NDFA

import org.burstsys.eql.generators.hydra.routes.StepTag

case class NDFATransition(tag: StepTag, node: NDFANode) {
    override def toString: String = {
      s"$tag->${node.hashCode()}"
    }

  }

