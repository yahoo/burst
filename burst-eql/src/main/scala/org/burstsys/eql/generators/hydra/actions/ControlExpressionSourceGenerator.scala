/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.actions

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.ControlExpression
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.{ActionSourceGenerator, _}

trait ControlExpressionSourceGenerator extends ActionSourceGenerator {
  self: ControlExpression =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val e = expression.generateSource()
    assert(e.length <= 1)
    e
  }

  override def phase(): ActionPhase = expression.phase()
}
