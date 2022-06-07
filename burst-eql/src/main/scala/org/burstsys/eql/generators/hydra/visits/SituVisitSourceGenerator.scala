/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.visits

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{ActionPhase, HydraVisitLanes}

trait SituVisitSourceGenerator extends PhasedVisitSourceGenerator {
  self: HydraVisitLanes =>

  override  def generatePrePost()(implicit cb: CodeBlock, context: GlobalContext): Unit = {
    val preWork = traverseLanes.map(_ generateSource ActionPhase.Pre).filter(_.nonEmpty)
    val postWork = traverseLanes.map(_ generateSource ActionPhase.Post).filter(_.nonEmpty)
    if (preWork.nonEmpty || postWork.nonEmpty) {
      s"situ => {".source
      // pre and post are combined into one for situ
      preWork.foreach(_.indent.source)
      postWork.foreach(_.indent.source)
      s"}".source
    }
  }
}
