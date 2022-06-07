/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.TemporaryFrequencyExpression
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.{ActionPhase, ActionSourceGenerator}
import org.burstsys.eql.generators.hydra.utils.CodeBlock

trait TemporaryFrequencyTempDeclSourceGenerator extends ActionSourceGenerator {
  self: TemporaryFrequencyExpression =>

  override def phase(): ActionPhase = ActionPhase.Pre

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    // this is just a dummy to force the declaration of the hold varaible
    CodeBlock()
  }
}
