/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql._
import org.burstsys.eql.actions.temporaries.TemporaryFrequencyExpression
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.{ActionPhase, ActionSourceGenerator, Var, toActionGenerator}
import org.burstsys.eql.generators.hydra.utils.{CodeBlock, _}

trait TemporaryFrequencyAfterSourceGenerator extends ActionSourceGenerator {
  self: TemporaryFrequencyExpression =>

  def aggTemporary: Var

  override def phase(): ActionPhase = ActionPhase.After

  // this generator controls the dimension inserts in it's visit
  override def providesDimensionWrite: Boolean = true

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = CodeBlock { implicit cb =>
    val surround = surrounding.expression.generateSource().head
    s"${qualifiedName(frequencyTargetName)} = $surround".source()
    s"${qualifiedName(dimensionTargetName)} = $name".source()
    s"insert($qualifiedFrameName)".source()
  }
}
