/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.temporaries

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.temporaries.TemporaryFrequencyExpression
import org.burstsys.eql.generators.ActionSourceGenerator
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.hydra.utils._

trait TemporaryFrequencySourceGenerator extends ActionSourceGenerator {
  self: TemporaryFrequencyExpression =>

  override
  def generateSource()(implicit context: GlobalContext): CodeBlock = CodeBlock { implicit cb =>
    s"$name = $name + 1".source
  }
}
