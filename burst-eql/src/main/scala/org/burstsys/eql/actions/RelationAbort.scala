/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.actions

import org.burstsys.eql.generators.ActionPhase
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.actions.RelationAbortSourceGenerator
import org.burstsys.motif.paths.Path

class RelationAbort(val path: Path) extends QueryAction with RelationAbortSourceGenerator {

  override def getLowestVisitPath: Path = path

  override def phase(): ActionPhase = ActionPhase.Post
}

object RelationAbort {
  def apply(path: Path):RelationAbort  = new RelationAbort(path)
}
